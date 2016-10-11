package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.unity3d.ads.log.DeviceLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRequestResultReceiver extends ResultReceiver {
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_FAILED = 2;

	private IWebRequestListener _listener;

	public WebRequestResultReceiver (Handler handler, IWebRequestListener listener) {
		super(handler);
		_listener = listener;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		DeviceLog.entered();

		if (_listener != null) {
			switch (resultCode) {
				case RESULT_SUCCESS:
					String url = resultData.getString("url");
					resultData.remove("url");
					String response = resultData.getString("response");
					resultData.remove("response");
					int responseCode = resultData.getInt("responseCode");
					resultData.remove("responseCode");
					_listener.onComplete(url, response, responseCode, getResponseHeaders(resultData));
					break;
				case RESULT_FAILED:
					_listener.onFailed(resultData.getString("url"), resultData.getString("error"));
					break;
				default:
					DeviceLog.error("Unhandled resultCode: " + resultCode);
					_listener.onFailed(resultData.getString("url"), "Invalid resultCode=" + resultCode);
					break;
			}
		}

		super.onReceiveResult(resultCode, resultData);
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
