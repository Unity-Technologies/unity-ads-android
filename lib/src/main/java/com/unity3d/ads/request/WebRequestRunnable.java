package com.unity3d.ads.request;

import android.os.Bundle;

import com.unity3d.ads.log.DeviceLog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRequestRunnable implements Runnable {
	private WebRequest _currentRequest;
	private boolean _canceled = false;
	private final String _url;
	private final String _type;
	private final String _body;
	private final int _connectTimeout;
	private final int _readTimeout;
	private final Map<String, List<String>> _headers;
	private final IWebRequestListener _listener;

	public WebRequestRunnable(String url, String type, String body, int connectTimeout, int readTimeout, Map<String, List<String>> headers, IWebRequestListener listener) {
		_url = url;
		_type = type;
		_body = body;
		_connectTimeout = connectTimeout;
		_readTimeout = readTimeout;
		_headers = headers;
		_listener = listener;
	}

	@Override public void run() {
		DeviceLog.debug("Handling request message: " + _url + " type=" + _type);
		try {
			makeRequest(_url, _type, _headers, _body, _connectTimeout, _readTimeout);
		}
		catch (MalformedURLException e) {
			DeviceLog.exception("Malformed URL", e);
			onFailed("Malformed URL");
		}
	}

	public void setCancelStatus (boolean canceled) {
		_canceled = canceled;

		if (_canceled && _currentRequest != null) {
			_currentRequest.cancel();
		}
	}

	private void makeRequest (String url, String type, Map<String, List<String>> headers, String body, int connectTimeout, int readTimeout) throws MalformedURLException {
		if (_canceled) {
			return;
		}

		_currentRequest = new WebRequest(url, type, headers, connectTimeout, readTimeout);

		if (body != null) {
			_currentRequest.setBody(body);
		}

		String response;
		try {
			response = _currentRequest.makeRequest();
		} catch (IOException | NetworkIOException | IllegalStateException e) {
			DeviceLog.exception("Error completing request", e);
			onFailed(e.getClass().getName() + ": " + e.getMessage());
			return;
		}

		if (!_currentRequest.isCanceled()) {
			Bundle data = new Bundle();

			for (String key : _currentRequest.getResponseHeaders().keySet()) {
				if (key == null || key.contentEquals("null")) {
					continue;
				}

				String[] values = new String[_currentRequest.getResponseHeaders().get(key).size()];
				for (int valueidx = 0; valueidx < _currentRequest.getResponseHeaders().get(key).size(); valueidx++) {
					values[valueidx] = _currentRequest.getResponseHeaders().get(key).get(valueidx);
				}

				data.putStringArray(key, values);
			}

			onSucceed(response, _currentRequest.getResponseCode(), getResponseHeaders(data));
		} else {
			onFailed("Canceled");
		}
	}

	private void onSucceed(String response, int responseCode, Map<String, List<String>> headers) {
		_listener.onComplete(_url, response, responseCode, headers);
	}

	private void onFailed(String error) {
		_listener.onFailed(_url, error);
	}

	private Map<String, List<String>> getResponseHeaders(Bundle resultData) {
		Map<String, List<String>> responseHeaders = null;
		if (resultData.size() > 0) {
			responseHeaders = new HashMap<>();
			for (String k : resultData.keySet()) {
				String[] tmpAr = resultData.getStringArray(k);
				if (tmpAr != null) {
					responseHeaders.put(k, new ArrayList<>(Arrays.asList(tmpAr)));
				}
			}
		}
		return responseHeaders;
	}
}
