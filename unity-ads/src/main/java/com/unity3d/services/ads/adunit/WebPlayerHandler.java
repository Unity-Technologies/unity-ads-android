package com.unity3d.services.ads.adunit;

import android.os.Bundle;
import android.view.View;

import com.unity3d.services.ads.webplayer.WebPlayerView;
import com.unity3d.services.ads.webplayer.WebPlayerSettingsCache;
import com.unity3d.services.ads.webplayer.WebPlayerViewCache;
import com.unity3d.services.core.misc.ViewUtilities;

public class WebPlayerHandler implements IAdUnitViewHandler {
	private WebPlayerView _webPlayerView;

	private static String webPlayerViewId = "webplayer";

	public boolean create(IAdUnitActivity activity) {
		if (_webPlayerView == null) {
			WebPlayerSettingsCache webPlayerSettingsCache = WebPlayerSettingsCache.getInstance();
			_webPlayerView = new WebPlayerView(activity.getContext(), webPlayerViewId, webPlayerSettingsCache.getWebSettings(webPlayerViewId), webPlayerSettingsCache.getWebPlayerSettings(webPlayerViewId));
			_webPlayerView.setEventSettings(webPlayerSettingsCache.getWebPlayerEventSettings(webPlayerViewId));
			WebPlayerViewCache.getInstance().addWebPlayer(webPlayerViewId, _webPlayerView);
		}

		return true;
	}

	public boolean destroy() {
		if (_webPlayerView != null) {
			ViewUtilities.removeViewFromParent(_webPlayerView);
			_webPlayerView.destroy();
		}

		WebPlayerViewCache.getInstance().removeWebPlayer(webPlayerViewId);
		_webPlayerView = null;

		return true;
	}

	public View getView() {
		return _webPlayerView;
	}

	public void onCreate(IAdUnitActivity activity, Bundle savedInstanceState) {
		this.create(activity);
	}

	public void onStart(IAdUnitActivity activity) {
	}

	public void onStop(IAdUnitActivity activity) {
	}

	public void onResume(IAdUnitActivity activity) {
	}

	public void onPause(IAdUnitActivity activity) {
	}

	public void onDestroy(IAdUnitActivity activity) {
		if (activity.isFinishing()) {
			destroy();
		}
	}
}
