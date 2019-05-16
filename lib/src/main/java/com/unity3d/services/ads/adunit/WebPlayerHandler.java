package com.unity3d.services.ads.adunit;

import android.os.Bundle;
import android.view.View;

import com.unity3d.services.ads.webplayer.WebPlayer;
import com.unity3d.services.core.misc.ViewUtilities;

public class WebPlayerHandler implements IAdUnitViewHandler {
	private WebPlayer _webPlayer;

	public boolean create(AdUnitActivity activity) {
		if (_webPlayer == null) {
			_webPlayer = new WebPlayer(activity, "webplayer", com.unity3d.services.ads.api.WebPlayer.getWebSettings(), com.unity3d.services.ads.api.WebPlayer.getWebPlayerSettings());
			_webPlayer.setEventSettings(com.unity3d.services.ads.api.WebPlayer.getWebPlayerEventSettings());
		}

		return true;
	}

	public boolean destroy() {
		if (_webPlayer != null) {
			ViewUtilities.removeViewFromParent(_webPlayer);
			_webPlayer.destroy();
		}

		_webPlayer = null;

		return true;
	}

	public View getView() {
		return _webPlayer;
	}

	public void onCreate(AdUnitActivity activity, Bundle savedInstanceState) {
		this.create(activity);
	}

	public void onStart(AdUnitActivity activity) {
	}

	public void onStop(AdUnitActivity activity) {
	}

	public void onResume(AdUnitActivity activity) {
	}

	public void onPause(AdUnitActivity activity) {
	}

	public void onDestroy(AdUnitActivity activity) {
		if (activity.isFinishing()) {
			destroy();
		}
	}
}
