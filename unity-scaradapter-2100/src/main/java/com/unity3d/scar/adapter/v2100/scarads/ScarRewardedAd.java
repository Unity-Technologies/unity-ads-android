package com.unity3d.scar.adapter.v2100.scarads;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v2100.requests.AdRequestFactory;

public class ScarRewardedAd extends ScarAdBase<RewardedAd> {

	public ScarRewardedAd(Context context, AdRequestFactory adRequestFactory, ScarAdMetadata scarAdMetadata, IAdsErrorHandler adsErrorHandler, IScarRewardedAdListenerWrapper adListener) {
		super(context, scarAdMetadata, adRequestFactory, adsErrorHandler);
		_scarAdListener = new ScarRewardedAdListener(adListener, this);
	}

	@Override
	protected void loadAdInternal(AdRequest adRequest, IScarLoadListener loadListener) {
		RewardedAd.load(_context, _scarAdMetadata.getAdUnitId(), adRequest, ((ScarRewardedAdListener)_scarAdListener).getAdLoadListener());
	}

	@Override
	public void show(Activity activity) {
		if (_adObj != null) {
			_adObj.show(activity, ((ScarRewardedAdListener)_scarAdListener).getOnUserEarnedRewardListener());
		} else {
			_adsErrorHandler.handleError(GMAAdsError.AdNotLoadedError(_scarAdMetadata));
		}
	}

}