package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import java.util.Timer;
import java.util.TimerTask;

public class ScarInterstitialAdHandler implements IScarInterstitialAdListenerWrapper {

	private ScarAdMetadata _scarAdMetadata;
	private  boolean _finishedPlaying = false;
	private boolean _hasSentStartEvents = false;
	private Timer _playbackTimer;
	private TimerTask _playbackTimerTask = new TimerTask() {
		@Override
		public void run() {
			_finishedPlaying = true;
		}
	};

	public ScarInterstitialAdHandler(ScarAdMetadata scarAdMetadata) {
		_scarAdMetadata = scarAdMetadata;
		_playbackTimer = new Timer();
	}

	@Override
	public void onAdLoaded() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_LOADED, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId());
	}

	@Override
	public void onAdFailedToLoad(int errorCode, String errorString) {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.LOAD_ERROR, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId(), errorString, errorCode);
	}

	@Override
	public void onAdOpened() {
		// We send all three events back to back since we don't have quartile callbacks from GMA
		if (!_hasSentStartEvents) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_STARTED);
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.FIRST_QUARTILE);
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.MIDPOINT);
			_hasSentStartEvents = true;
		}
		// Start timer here to see if the user watched to completion
		_finishedPlaying = false;
		_playbackTimer.schedule(_playbackTimerTask, _scarAdMetadata.getVideoLengthMs());
	}

	@Override
	public void onAdFailedToShow(int errorCode, String errorString) {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.INTERSTITIAL_SHOW_ERROR, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId(), errorString, errorCode);
	}

	@Override
	public void onAdClicked() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_CLICKED);
	}

	@Override
	public void onAdClosed() {
		if (!_finishedPlaying) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_SKIPPED);
			_playbackTimer.cancel();
		}
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_CLOSED);
	}

	@Override
	public void onAdLeftApplication() {
	}

	@Override
	public void onAdImpression() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.INTERSTITIAL_IMPRESSION_RECORDED, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId());
	}

}
