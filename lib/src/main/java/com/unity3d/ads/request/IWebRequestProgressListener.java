package com.unity3d.ads.request;

import java.util.List;
import java.util.Map;

public interface IWebRequestProgressListener {
	void onRequestStart(String url, long total, int responseCode, Map<String, List<String>> headers);
	void onRequestProgress(String url, long bytes, long total);
}