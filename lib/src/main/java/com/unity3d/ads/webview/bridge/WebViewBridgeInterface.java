package com.unity3d.ads.webview.bridge;

import android.webkit.JavascriptInterface;

import com.unity3d.ads.log.DeviceLog;

import org.json.JSONArray;
import org.json.JSONException;

public class WebViewBridgeInterface {
	@JavascriptInterface
	public void handleInvocation(String data) throws JSONException {
		DeviceLog.debug("handleInvocation " + data);

		JSONArray invocationArray = new JSONArray(data);
		Invocation batch = new Invocation();

		for (int idx = 0; idx < invocationArray.length(); idx++) {
			JSONArray currentInvocation = (JSONArray)invocationArray.get(idx);
			String className = (String)currentInvocation.get(0);
			String methodName = (String)currentInvocation.get(1);
			JSONArray parameters = (JSONArray)currentInvocation.get(2);
			String callback = (String)currentInvocation.get(3);

			batch.addInvocation(className, methodName, getParameters(parameters), new WebViewCallback(callback, batch.getId()));
		}

		for (int idx = 0; idx < invocationArray.length(); idx++) {
			batch.nextInvocation();
		}

		batch.sendInvocationCallback();
	}

	@JavascriptInterface
	public void handleCallback(String callbackId, String callbackStatus, String rawParameters) throws Exception {
		DeviceLog.debug("handleCallback " + callbackId + " " + callbackStatus + " " + rawParameters);

		JSONArray parametersJsonArray = new JSONArray(rawParameters);
		Object[] parameters = null;
		if(parametersJsonArray.length() > 0) {
			parameters = new Object[parametersJsonArray.length()];
			for(int i = 0; i < parametersJsonArray.length(); ++i) {
				parameters[i] = parametersJsonArray.get(i);
			}
		}

		WebViewBridge.handleCallback(callbackId, callbackStatus, parameters);
	}

	private Object[] getParameters (JSONArray parametersJson) throws JSONException {
		Object[] parameters = new Object[parametersJson.length()];
		for(int i = 0; i < parametersJson.length(); ++i) {
			parameters[i] = parametersJson.get(i);
		}

		return parameters;
	}
}