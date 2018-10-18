package com.unity3d.services.ads.api;

import android.view.View;

import com.unity3d.services.ads.adunit.IAdUnitViewHandler;
import com.unity3d.services.banners.view.BannerView;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.ads.webplayer.WebPlayerError;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import static com.unity3d.services.core.misc.Utilities.runOnUiThread;

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
	public static void setUrl (final String url, String viewId, final WebViewCallback callback) {
		final com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getWebPlayer(viewId);
		if (webPlayer != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayer.loadUrl(url);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setData (final String data, final String mimeType, final String encoding, String viewId, final WebViewCallback callback) {
		final com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getWebPlayer(viewId);
		if (webPlayer != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayer.loadData(data, mimeType, encoding);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setDataWithUrl (final String baseUrl, final String data, final String mimeType, final String encoding, String viewId, final WebViewCallback callback) {
		final com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getWebPlayer(viewId);
		if (webPlayer != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayer.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, null);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setSettings (final JSONObject webSettings, final JSONObject webPlayerSettings, String viewId, final WebViewCallback callback) {
		switch (viewId) {
			case "bannerplayer":
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getBannerWebPlayer();
						if (webPlayer != null) {
							webPlayer.setSettings(webSettings, webPlayerSettings);
						} else {
							BannerView.setWebPlayerSettings(webSettings, webPlayerSettings);
						}
					}
				});
				break;
			case "webplayer":
				_webSettings = webSettings;
				_webPlayerSettings = webPlayerSettings;
				break;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void setEventSettings (final JSONObject settings, String viewId, final WebViewCallback callback) {
		if (viewId.equals("webplayer")) {
			_webPlayerEventSettings = settings;
		} else {
            runOnUiThread(new Runnable() {
				@Override
				public void run() {
					com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getBannerWebPlayer();
					if (webPlayer != null) {
					    webPlayer.setEventSettings(settings);
					} else {
						BannerView.setWebPlayerEventSettings(settings);
					}
				}
			});

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
	public static void getErroredSettings (String viewId, final WebViewCallback callback) {
		com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getWebPlayer(viewId);
		if (webPlayer != null) {
			Map<String, String> errors = webPlayer.getErroredSettings();
			JSONObject retObj = new JSONObject();

			try {
				Iterator errorIterator = errors.entrySet().iterator();
				while (errorIterator.hasNext()) {
					Map.Entry pair = (Map.Entry)errorIterator.next();
					retObj.put((String)pair.getKey(), pair.getValue());
				}
			} catch (Exception e) {
				DeviceLog.exception("Error forming JSON object", e);
			}

			callback.invoke(retObj);
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void sendEvent(final JSONArray parameters, String viewId, final WebViewCallback callback) {
		com.unity3d.services.ads.webplayer.WebPlayer webPlayer = getWebPlayer(viewId);
		if (webPlayer != null) {
			webPlayer.sendEvent(parameters);
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	private static com.unity3d.services.ads.webplayer.WebPlayer getWebPlayer(String viewId) {
		switch (viewId) {
            case "webplayer":
                return getAdUnitWebPlayer();
            case "bannerplayer":
                return getBannerWebPlayer();
            default:
                return null;
		}
	}

	private static com.unity3d.services.ads.webplayer.WebPlayer getAdUnitWebPlayer() {
	    if (AdUnit.getAdUnitActivity() != null) {
			IAdUnitViewHandler viewHandler = AdUnit.getAdUnitActivity().getViewHandler("webplayer");
			if (viewHandler != null) {
				View view = viewHandler.getView();
				if (view != null) {
					return (com.unity3d.services.ads.webplayer.WebPlayer)view;
				}
			}
		}

		return null;
	}

	private static com.unity3d.services.ads.webplayer.WebPlayer getBannerWebPlayer() {
		if (BannerView.getInstance() == null) {
			return null;
		}
	    return BannerView.getInstance().getWebPlayer();
	}
}
