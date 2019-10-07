package com.unity3d.services.ads.api;

import android.os.Build;

import com.unity3d.services.ads.webplayer.WebPlayerEventBridge;
import com.unity3d.services.ads.webplayer.WebPlayerSettingsCache;
import com.unity3d.services.ads.webplayer.WebPlayerViewCache;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.ads.webplayer.WebPlayerError;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import static com.unity3d.services.core.misc.Utilities.runOnUiThread;

public class WebPlayer {

	@WebViewExposed
	public static void setUrl(final String url, String viewId, final WebViewCallback callback) {
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayerView.loadUrl(url);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setData(final String data, final String mimeType, final String encoding, String viewId, final WebViewCallback callback) {
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayerView.loadData(data, mimeType, encoding);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setDataWithUrl(final String baseUrl, final String data, final String mimeType, final String encoding, String viewId, final WebViewCallback callback) {
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayerView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, null);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setSettings(final JSONObject webSettings, final JSONObject webPlayerSettings, final String viewId, final WebViewCallback callback) {
		// Update WebPlayerSettingsCache with updated settings
		WebPlayerSettingsCache.getInstance().addWebSettings(viewId, webSettings);
		WebPlayerSettingsCache.getInstance().addWebPlayerSettings(viewId, webPlayerSettings);
		// Update WebPlayerView with updated settings if there is a view
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayerView.setSettings(webSettings, webPlayerSettings);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void setEventSettings(final JSONObject eventSettings, String viewId, final WebViewCallback callback) {
		// Update WebPlayerSettingsCache with updated event settings
		WebPlayerSettingsCache.getInstance().addWebPlayerEventSettings(viewId, eventSettings);
		// Update WebPlayerView with updated settings if there is a view
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webPlayerView.setEventSettings(eventSettings);
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void clearSettings(String viewId, final WebViewCallback callback) {
		WebPlayerSettingsCache webPlayerSettingsCache = WebPlayerSettingsCache.getInstance();
		webPlayerSettingsCache.removeWebSettings(viewId);
		webPlayerSettingsCache.removeWebPlayerSettings(viewId);
		webPlayerSettingsCache.removeWebPlayerEventSettings(viewId);
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// This will not actually remove the previous settings
					// Leaving in for consistency with iOS code, once settings in WebPlayer is handled differently this will be fixed.
					webPlayerView.setSettings(new JSONObject(), new JSONObject());
					webPlayerView.setEventSettings(new JSONObject());
				}
			});
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void getErroredSettings(String viewId, final WebViewCallback callback) {
		com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			Map<String, String> errors = webPlayerView.getErroredSettings();
			JSONObject retObj = new JSONObject();

			try {
				Iterator errorIterator = errors.entrySet().iterator();
				while (errorIterator.hasNext()) {
					Map.Entry pair = (Map.Entry) errorIterator.next();
					retObj.put((String) pair.getKey(), pair.getValue());
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
		com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			webPlayerView.sendEvent(parameters);
			callback.invoke();
		} else {
			callback.error(WebPlayerError.WEBPLAYER_NULL);
		}
	}

	@WebViewExposed
	public static void getFrame(final String callId, final String viewId, final WebViewCallback callback) {
		callback.invoke();
		final com.unity3d.services.ads.webplayer.WebPlayerView webPlayerView = WebPlayerViewCache.getInstance().getWebPlayer(viewId);
		if (webPlayerView != null) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int[] location = new int[2];
					webPlayerView.getLocationOnScreen(location);
					int x = location[0];
					int y = location[1];
					int width = webPlayerView.getWidth();
					int height = webPlayerView.getHeight();
					float alpha = 1.0f;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						alpha = webPlayerView.getAlpha();
					}
					WebPlayerEventBridge.sendGetFrameResponse(callId, viewId, x, y, width, height, alpha);
				}
			});
		}
	}

}
