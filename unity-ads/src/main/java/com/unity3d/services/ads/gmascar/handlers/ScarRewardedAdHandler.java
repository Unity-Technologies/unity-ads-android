package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
import com.unity3d.services.core.misc.EventSubject;

public class ScarRewardedAdHandler extends ScarAdHandlerBase implements IScarRewardedAdListenerWrapper {

	private boolean _hasEarnedReward = false;

	public ScarRewardedAdHandler(ScarAdMetadata scarAdMetadata, EventSubject<GMAEvent> eventSubject, GMAEventSender gmaEventSender) {
		super(scarAdMetadata, eventSubject, gmaEventSender);
	}

	@Override
	public void onAdFailedToShow(int errorCode, String errorString) {
		_gmaEventSender.send(GMAEvent.REWARDED_SHOW_ERROR, _scarAdMetadata.getPlacementId(), _scarAdMetadata.getQueryId(), errorString, errorCode);
	}

	@Override
	public void onUserEarnedReward() {
		_hasEarnedReward = true;
		_gmaEventSender.send(GMAEvent.AD_EARNED_REWARD);
	}

	@Override
	public void onAdSkipped() {
		_gmaEventSender.send(GMAEvent.AD_SKIPPED);
	}

	@Override
	public void onAdClosed() {
		if (!_hasEarnedReward) {
			onAdSkipped();
		}
		super.onAdClosed();
	}

	@Override
	public void onAdImpression() {
		_gmaEventSender.send(GMAEvent.REWARDED_IMPRESSION_RECORDED);
	}
}
