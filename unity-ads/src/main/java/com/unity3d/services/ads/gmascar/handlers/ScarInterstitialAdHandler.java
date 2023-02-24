package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
import com.unity3d.services.core.misc.EventSubject;

public class ScarInterstitialAdHandler extends ScarAdHandlerBase implements IScarInterstitialAdListenerWrapper {

	public ScarInterstitialAdHandler(ScarAdMetadata scarAdMetadata, EventSubject<GMAEvent> eventSubject, GMAEventSender gmaEventSender) {
		super(scarAdMetadata, eventSubject, gmaEventSender);
	}

	@Override
	public void onAdFailedToShow(int errorCode, String errorString) {
		_gmaEventSender.send(GMAEvent.INTERSTITIAL_SHOW_ERROR, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId(), errorString, errorCode);
	}

	@Override
	public void onAdClosed() {
		if (!_eventSubject.eventQueueIsEmpty()) {
			super.onAdSkipped();
		}
		super.onAdClosed();
	}

	@Override
	public void onAdLeftApplication() {
		_gmaEventSender.send(GMAEvent.AD_LEFT_APPLICATION);
	}

	@Override
	public void onAdImpression() {
		_gmaEventSender.send(GMAEvent.INTERSTITIAL_IMPRESSION_RECORDED);
	}
}
