package com.unity3d.ads.api;

import com.unity3d.ads.adunit.AdUnitError;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.webplayer.WebPlayerError;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class WebPlayer {
	private static JSONObject _webSettings = null;
	private static JSONObject _webPlayerSettings = null;
	private static JSONObject _webPlayerEventSettings = null;

	public static JSONObject getWebPlayerSettings () {
		return _webPlayerSettings;
	}

	public static JSONObject getWebSettings () {
		return _webSettings;
	}

	public static JSONObject getWebPlayerEventSettings () {
		return _webPlayerEventSettings;
	}

	@WebViewExposed
	public static void setUrl (final String url, final WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() != null) {
			if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
				Utilities.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
							AdUnit.getAdUnitActivity().getWebPlayer().loadUrl(url);
						}
					}
				});

				callback.invoke();
			}
			else {
				callback.error(WebPlayerError.WEBPLAYER_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void setData (final String data, final String mimeType, final String encoding, final WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() != null) {
			if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
				Utilities.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
							AdUnit.getAdUnitActivity().getWebPlayer().loadData(data, mimeType, encoding);
						}
					}
				});
				callback.invoke();
			}
			else {
				callback.error(WebPlayerError.WEBPLAYER_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void setDataWithUrl (final String baseUrl, final String data, final String mimeType, final String encoding, final WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() != null) {
			if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
				Utilities.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
							AdUnit.getAdUnitActivity().getWebPlayer().loadDataWithBaseURL(baseUrl, data, mimeType, encoding, null);
						}
					}
				});
				callback.invoke();
			}
			else {
				callback.error(WebPlayerError.WEBPLAYER_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void setSettings (final JSONObject webSettings, final JSONObject webPlayerSettings, final WebViewCallback callback) {
		_webSettings = webSettings;
		_webPlayerSettings = webPlayerSettings;

		callback.invoke();
	}

	@WebViewExposed
	public static void setEventSettings (final JSONObject settings, final WebViewCallback callback) {
		_webPlayerEventSettings = settings;

		if (AdUnit.getAdUnitActivity() != null) {
			if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
				AdUnit.getAdUnitActivity().getWebPlayer().setEventSettings(settings);
			}
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void clearSettings (final WebViewCallback callback) {
		_webSettings = null;
		_webPlayerSettings = null;
		_webPlayerEventSettings = null;

		callback.invoke();
	}

	@WebViewExposed
	public static void getErroredSettings (final WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() != null) {
			if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
				Map<String, String> errors = AdUnit.getAdUnitActivity().getWebPlayer().getErroredSettings();
				JSONObject retObj = new JSONObject();

				try {
					Iterator errorIterator = errors.entrySet().iterator();
					while (errorIterator.hasNext()) {
						Map.Entry pair = (Map.Entry)errorIterator.next();
						retObj.put((String)pair.getKey(), pair.getValue());
					}
				}
				catch (Exception e) {
					DeviceLog.exception("Error forming JSON object", e);
				}

				callback.invoke(retObj);
			}
			else {
				callback.error(WebPlayerError.WEBPLAYER_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}

	@WebViewExposed
	public static void sendEvent(final JSONArray parameters, final WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() != null) {
			if (AdUnit.getAdUnitActivity().getWebPlayer() != null) {
			    AdUnit.getAdUnitActivity().getWebPlayer().sendEvent(parameters);
				callback.invoke();
			}
			else {
				callback.error(WebPlayerError.WEBPLAYER_NULL);
			}
		}
		else {
			callback.error(AdUnitError.ADUNIT_NULL);
		}
	}
}
