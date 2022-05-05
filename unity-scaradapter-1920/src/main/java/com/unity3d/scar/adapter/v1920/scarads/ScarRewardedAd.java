package com.unity3d.scar.adapter.v1920.scarads;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v1920.signals.QueryInfoMetadata;

public class ScarRewardedAd extends ScarAdBase {

	private RewardedAd _rewardedAd;
	private ScarRewardedAdListener _rewardedAdDelegate;

	public ScarRewardedAd(Context context, QueryInfoMetadata queryInfoMetadata, ScarAdMetadata scarAdMetadata, IAdsErrorHandler adsErrorHandler, IScarRewardedAdListenerWrapper adListener) {
		super(context, scarAdMetadata, queryInfoMetadata, adsErrorHandler);
		_rewardedAd = new RewardedAd(_context, _scarAdMetadata.getAdUnitId());
		_rewardedAdDelegate = new ScarRewardedAdListener(_rewardedAd, adListener);
	}

	@Override
	public void loadAdInternal(IScarLoadListener loadListener, AdRequest adRequest) {
		_rewardedAdDelegate.setLoadListener(loadListener);
		_rewardedAd.loadAd(adRequest, _rewardedAdDelegate.getRewardedAdLoadCallback());
	}

	@Override
	public void show(Activity activity) {
		if (_rewardedAd.isLoaded()) {
			_rewardedAd.show(activity, _rewardedAdDelegate.getRewardedAdCallback());
		} else {
			_adsErrorHandler.handleError(GMAAdsError.AdNotLoadedError(_scarAdMetadata));
		}
	}

}