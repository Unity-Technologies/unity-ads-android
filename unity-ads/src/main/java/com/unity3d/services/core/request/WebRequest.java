package com.unity3d.services.core.request;

import com.unity3d.services.core.log.DeviceLog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class WebRequest {
	private URL _url;
	private String _requestType;
	private byte[] _body;
	private Map<String, List<String>> _headers;
	private Map<String, List<String>> _responseHeaders;
	private int _responseCode = -1;
	private long _contentLength = -1;
	private boolean _canceled = false;
	private ByteArrayOutputStream _baos;

	private int _connectTimeout;
	private int _readTimeout;

	private IWebRequestProgressListener _progressListener;

	public enum RequestType {
		POST,
		GET,
		HEAD
	}

	public WebRequest (String url, String requestType) throws MalformedURLException {
		this(url, requestType, null);
	}

	public WebRequest (String url, String requestType, Map<String, List<String>> headers) throws MalformedURLException {
		this(url, requestType, headers, 30000, 30000);
	}

	public WebRequest (String url, String requestType, Map<String, List<String>> headers, int connectTimeout, int readTimeout) throws MalformedURLException {
		_url = new URL(url);
		_requestType = requestType;
		_headers = headers;
		_connectTimeout = connectTimeout;
		_readTimeout = readTimeout;
	}

	public void cancel () {
		_canceled = true;
	}

	public boolean isCanceled () {
		return _canceled;
	}

	public URL getUrl () {
		return _url;
	}

	public String getRequestType () {
		return _requestType;
	}

	public byte[] getBody () {
		return _body;
	}

	public void setBody (String body) {
		_body = body.getBytes(StandardCharsets.UTF_8);
	}

	public void setBody (byte[] body) {
		_body = body;
	}

	public String getQuery () {
		if (_url != null) {
			return _url.getQuery();
		}

		return null;
	}

	public Map<String, List<String>> getResponseHeaders () {
		return _responseHeaders;
	}

	public Map<String, List<String>> getHeaders () {
		return _headers;
	}

	public int getResponseCode () {
		return _responseCode;
	}

	public long getContentLength() { return _contentLength; }

	public int getConnectTimeout () {
		return _connectTimeout;
	}

	public void setConnectTimeout (int timeout) {
		_connectTimeout = timeout;
	}

	public int getReadTimeout () {
		return _readTimeout;
	}

	public void setReadTimeout (int readTimeout) {
		_readTimeout = readTimeout;
	}

	public void setProgressListener(IWebRequestProgressListener listener) {
		_progressListener = listener;
	}

	public long makeStreamRequest(OutputStream outputStream) throws Exception {
		HttpURLConnection connection = getHttpUrlConnectionWithHeaders();
		connection.setDoInput(true);

		if (getRequestType().equals(RequestType.POST.name())) {
			connection.setDoOutput(true);
			OutputStream pout = null;

			try {
				pout = connection.getOutputStream();
				if (getBody() == null) {
					String query = getQuery();
					if (query != null) {
						pout.write(query.getBytes(StandardCharsets.UTF_8));
					}
				}
				else {
					pout.write(getBody());
				}

				pout.flush();
			}
			catch (IOException e) {
				DeviceLog.exception("Error while writing POST params", e);
				throw new NetworkIOException("Error writing POST params: " + e.getMessage());
			}
			finally {
				try {
					if (pout != null) {
						pout.close();
					}
				}
				catch (IOException e) {
					DeviceLog.exception("Error closing writer", e);
				}
			}
		}

		try {
			_responseCode = connection.getResponseCode();
		}
		catch (IOException | RuntimeException e) {
			throw new NetworkIOException("Response code: " + e.getMessage());
		}

		_contentLength = connection.getContentLength();
		if (_contentLength == -1) {
			// X-OrigLength is a custom field which contains uncompressed response size.
			// More information here https://unity.slack.com/archives/CULLDMTH9/p1589991686357100
			_contentLength = connection.getHeaderFieldInt("X-OrigLength", -1);
		}

		// If we know expected content size and the call was made from  String makeRequest(),
		// override already created boas with new stream with preallocated buffer.
		if (_baos != null && _baos == outputStream && _contentLength > 0) {
			_baos = new ByteArrayOutputStream((int)_contentLength);
			outputStream = _baos;
		}

		if (connection.getHeaderFields() != null) {
			_responseHeaders = connection.getHeaderFields();
		}

		InputStream input;
		try {
			input = connection.getInputStream();
		} catch (IOException e) {
			input = connection.getErrorStream();
			if(input == null) {
				throw new NetworkIOException("Can't open error stream: " + e.getMessage());
			}
		}

		if(_progressListener != null) {
			_progressListener.onRequestStart(getUrl().toString(), _contentLength, _responseCode, _responseHeaders);
		}

		BufferedInputStream binput = new BufferedInputStream(input);
		int bytesRead = 0;
		long total = 0;
		byte[] readTarget = new byte[4096];

		while (!isCanceled() && bytesRead != -1) {
			try {
				bytesRead = binput.read(readTarget);
			}
			catch (IOException e) {
				throw new NetworkIOException("Network exception: " + e.getMessage());
			}
			catch (Exception e) {
				throw new Exception("Unknown Exception: " + e.getMessage());
			}

			if (bytesRead > 0) {
				outputStream.write(readTarget, 0, bytesRead);
				total += bytesRead;

				if(_progressListener != null) {
					_progressListener.onRequestProgress(getUrl().toString(), total, _contentLength);
				}
			}
		}

		connection.disconnect();
		outputStream.flush();

		return total;
	}

	public String makeRequest () throws Exception {
		_baos = new ByteArrayOutputStream();
		makeStreamRequest(_baos);
		return _baos.toString("UTF-8");
	}

	private HttpURLConnection getHttpUrlConnectionWithHeaders() throws NetworkIOException, IllegalArgumentException {
		HttpURLConnection connection;

		if (getUrl().toString().startsWith("https://")) {
			try {
				connection = (HttpsURLConnection)getUrl().openConnection();
			}
			catch (IOException e) {
				throw new NetworkIOException("Open HTTPS connection: " + e.getMessage());
			}
		}
		else if (getUrl().toString().startsWith("http://")) {
			try {
				connection = (HttpURLConnection)getUrl().openConnection();
			}
			catch (IOException e) {
				throw new NetworkIOException("Open HTTP connection: " + e.getMessage());
			}
		}
		else {
			throw new IllegalArgumentException("Invalid url-protocol in url: " + getUrl().toString());
		}

		connection.setInstanceFollowRedirects(false);
		connection.setConnectTimeout(getConnectTimeout());
		connection.setReadTimeout(getReadTimeout());

		try {
			connection.setRequestMethod(getRequestType());
		}
		catch (ProtocolException e) {
			throw new NetworkIOException("Set Request Method: " + getRequestType() + ", " + e.getMessage());
		}

		if (getHeaders() != null && getHeaders().size() > 0) {
			for (String k : getHeaders().keySet()) {
				for (String value : getHeaders().get(k)) {
					DeviceLog.debug("Setting header: " + k + "=" + value);
					connection.setRequestProperty(k, value);
				}
			}
		}

		return connection;
	}
}
