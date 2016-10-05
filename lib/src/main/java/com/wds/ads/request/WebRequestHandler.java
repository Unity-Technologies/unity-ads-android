package com.wds.ads.request;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wds.ads.log.DeviceLog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WebRequestHandler extends Handler {
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

	private void makeRequest (String url, String type, HashMap<String, List<String>> headers, String body, int connectTimeout, int readTimeout, WebRequestResultReceiver receiver) throws MalformedURLException {
		WebRequest request = new WebRequest(url, type, headers, connectTimeout, readTimeout);

		if (body != null) {
			request.setBody(body);
		}

		String response;
		try {
			response = request.makeRequest();
		} catch (IOException e) {
			DeviceLog.exception("Error completing request", e);
			receiver.send(WebRequestResultReceiver.RESULT_FAILED, getBundleForFailResult(url, e.getMessage(), type, body));
			return;
		}

		Bundle data = new Bundle();
		data.putString("response", response);
		data.putString("url", url);
		data.putInt("responseCode", request.getResponseCode());

		for (String key : request.getResponseHeaders().keySet()) {
			if (key == null || key.contentEquals("null")) {
				continue;
			}

			String[] values = new String[request.getResponseHeaders().get(key).size()];
			for (int valueidx = 0; valueidx < request.getResponseHeaders().get(key).size(); valueidx++) {
				values[valueidx] = request.getResponseHeaders().get(key).get(valueidx);
			}

			data.putStringArray(key, values);
		}

		receiver.send(WebRequestResultReceiver.RESULT_SUCCESS, data);
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
