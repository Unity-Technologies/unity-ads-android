package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.unity3d.ads.log.DeviceLog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WebRequestHandler extends Handler {
	private WebRequest _currentRequest;
	private boolean _canceled = false;

	@Override
	public void handleMessage (Message msg) {
		Bundle data = msg.getData();
		String url = data.getString("url");
		data.remove("url");
		String type = data.getString("type");
		data.remove("type");
		String body = data.getString("body");
		data.remove("body");
		WebRequestResultReceiver receiver = data.getParcelable("receiver");
		data.remove("receiver");
		int connectTimeout = data.getInt("connectTimeout");
		data.remove("connectTimeout");
		int readTimeout = data.getInt("readTimeout");
		data.remove("readTimeout");

		HashMap<String, List<String>> headers = null;
		if (data.size() > 0) {
			DeviceLog.debug("There are headers left in data, reading them");
			headers = new HashMap<>();
			List<String> values;

			for (String k : data.keySet()) {
				values = Arrays.asList(data.getStringArray(k));
				headers.put(k, values);
			}
		}

		if (msg.what == WebRequestThread.MSG_REQUEST) {
			DeviceLog.debug("Handling request message: " + url + " type=" + type);
			try {
				makeRequest(url, type, headers, body, connectTimeout, readTimeout, receiver);
			}
			catch (MalformedURLException e) {
				DeviceLog.exception("Malformed URL", e);
				if (receiver != null) {
					receiver.send(WebRequestResultReceiver.RESULT_FAILED, getBundleForFailResult(url, "Malformed URL", type, body));
				}
			}
		} else {
			DeviceLog.error("No implementation for message: " + msg.what);
			if (receiver != null) {
				receiver.send(WebRequestResultReceiver.RESULT_FAILED, getBundleForFailResult(url, "Invalid Thread Message", type, body));
			}
		}
	}

	public void setCancelStatus (boolean canceled) {
		_canceled = canceled;

		if (_canceled && _currentRequest != null) {
			_currentRequest.cancel();
		}
	}

	private void makeRequest (String url, String type, HashMap<String, List<String>> headers, String body, int connectTimeout, int readTimeout, WebRequestResultReceiver receiver) throws MalformedURLException {
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
			receiver.send(WebRequestResultReceiver.RESULT_FAILED, getBundleForFailResult(url, e.getClass().getName() + ": " + e.getMessage(), type, body));
			return;
		}

		if (!_currentRequest.isCanceled()) {
			Bundle data = new Bundle();
			data.putString("response", response);
			data.putString("url", url);
			data.putInt("responseCode", _currentRequest.getResponseCode());

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

			receiver.send(WebRequestResultReceiver.RESULT_SUCCESS, data);
		}
	}

	private Bundle getBundleForFailResult (String url, String error, String type, String body) {
		Bundle data = new Bundle();
		data.putString("url", url);
		data.putString("error", error);
		data.putString("type", type);
		data.putString("body", body);

		return data;
	}
}
