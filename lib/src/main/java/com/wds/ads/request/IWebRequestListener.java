package com.wds.ads.request;

import java.util.List;
import java.util.Map;

public interface IWebRequestListener {
	void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers);
	void onFailed(String url, String error);
}
