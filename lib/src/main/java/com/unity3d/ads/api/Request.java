package com.unity3d.ads.api;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.request.IWebRequestListener;
import com.unity3d.ads.request.WebRequest;
import com.unity3d.ads.request.WebRequestError;
import com.unity3d.ads.request.WebRequestEvent;
import com.unity3d.ads.request.WebRequestThread;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
	@WebViewExposed
	public static void get (final String id, String url, JSONArray headers, Integer connectTimeout, Integer readTimeout, final WebViewCallback callback) {
		if (headers != null && headers.length() == 0) {
			headers = null;
		}

		HashMap<String, List<String>> mappedHeaders;
		try {
			mappedHeaders = getHeadersMap(headers);
		}
		catch (Exception e) {
			DeviceLog.exception("Error mapping headers for the request", e);
			callback.error(WebRequestError.MAPPING_HEADERS_FAILED, id);
			return;
		}

		WebRequestThread.request(url, WebRequest.RequestType.GET, mappedHeaders, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> responseHeaders) {
				JSONArray headers;
				try {
					headers = getResponseHeadersMap(responseHeaders);
				}
				catch (Exception e) {
					DeviceLog.exception("Error parsing response headers", e);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.FAILED, id, url, "Error parsing response headers");
					return;
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.COMPLETE, id, url, response, responseCode, headers);
			}

			@Override
			public void onFailed(String url, String error) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.FAILED, id, url, error);
			}
		});

		callback.invoke(id);
	}

	@WebViewExposed
	public static void post (final String id, String url, String requestBody, JSONArray headers, Integer connectTimeout, Integer readTimeout, final WebViewCallback callback) {
		if (requestBody != null && requestBody.length() == 0) {
			requestBody = null;
		}
		if (headers != null && headers.length() == 0) {
			headers = null;
		}

		HashMap<String, List<String>> mappedHeaders;
		try {
			mappedHeaders = getHeadersMap(headers);
		}
		catch (Exception e) {
			DeviceLog.exception("Error mapping headers for the request", e);
			callback.error(WebRequestError.MAPPING_HEADERS_FAILED, id);
			return;
		}

		WebRequestThread.request(url, WebRequest.RequestType.POST, mappedHeaders, requestBody, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> responseHeaders) {
				JSONArray headers;
				try {
					headers = getResponseHeadersMap(responseHeaders);
				}
				catch (Exception e) {
					DeviceLog.exception("Error parsing response headers", e);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.FAILED, id, url, "Error parsing response headers");
					return;
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.COMPLETE, id, url, response, responseCode, headers);
			}

			@Override
			public void onFailed(String url, String error) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.FAILED, id, url, error);
			}
		});

		callback.invoke(id);
	}

	@WebViewExposed
	public static void head(final String id, String url, JSONArray headers, Integer connectTimeout, Integer readTimeout, final WebViewCallback callback) {
		if (headers != null && headers.length() == 0) {
			headers = null;
		}

		HashMap<String, List<String>> mappedHeaders;
		try {
			mappedHeaders = getHeadersMap(headers);
		}
		catch (Exception e) {
			DeviceLog.exception("Error mapping headers for the request", e);
			callback.error(WebRequestError.MAPPING_HEADERS_FAILED, id);
			return;
		}

		WebRequestThread.request(url, WebRequest.RequestType.HEAD, mappedHeaders, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> responseHeaders) {
				JSONArray headers;
				try {
					headers = getResponseHeadersMap(responseHeaders);
				}
				catch (Exception e) {
					DeviceLog.exception("Error parsing response headers", e);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.FAILED, id, url, "Error parsing response headers");
					return;
				}
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.COMPLETE, id, url, response, responseCode, headers);
			}

			@Override
			public void onFailed(String url, String error) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.REQUEST, WebRequestEvent.FAILED, id, url, error);
			}
		});

		callback.invoke(id);
	}

	@WebViewExposed
	public static void setConcurrentRequestCount(Integer count, final WebViewCallback callback) {
		WebRequestThread.setConcurrentRequestCount(count);
		callback.invoke();
	}

	@WebViewExposed
	public static void setMaximumPoolSize(Integer count, final WebViewCallback callback) {
		WebRequestThread.setMaximumPoolSize(count);
		callback.invoke();
	}

	@WebViewExposed
	public static void setKeepAliveTime(Integer milliseconds, final WebViewCallback callback) {
		WebRequestThread.setKeepAliveTime(milliseconds.longValue());
		callback.invoke();
	}

	public static JSONArray getResponseHeadersMap (Map<String, List<String>> responseHeaders) {
		JSONArray retObj = new JSONArray();

		if (responseHeaders != null && responseHeaders.size() > 0) {
			for (String key : responseHeaders.keySet()) {
				JSONArray keyValueMap = null;

				for (String value : responseHeaders.get(key)) {
					keyValueMap = new JSONArray();
					keyValueMap.put(key);
					keyValueMap.put(value);
				}

				retObj.put(keyValueMap);
			}
		}

		return retObj;
	}

	public static HashMap<String, List<String>> getHeadersMap (JSONArray headers) throws JSONException {
		HashMap<String, List<String>> mappedHeaders = null;

		if (headers != null) {
			mappedHeaders = new HashMap<>();
			for (int idx = 0; idx < headers.length(); idx++) {
				JSONArray header = (JSONArray)headers.get(idx);
				List<String> valueList = mappedHeaders.get(header.getString(0));
				if (valueList == null) {
					valueList = new ArrayList<>();
				}

				valueList.add(header.getString(1));
				mappedHeaders.put(header.getString(0), valueList);
			}
		}

		return mappedHeaders;
	}

}
