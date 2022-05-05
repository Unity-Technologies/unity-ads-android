package com.unity3d.scar.adapter.v1950.scarads;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;

public class ScarRewardedAdListener {

	private RewardedAd _rewardedAd;
	private IScarRewardedAdListenerWrapper _adListenerWrapper;
	private IScarLoadListener _loadListener;

	public ScarRewardedAdListener(RewardedAd rewardedAd, IScarRewardedAdListenerWrapper adListenerWrapper) {
		_rewardedAd = rewardedAd;
		_adListenerWrapper = adListenerWrapper;
	}

	private RewardedAdLoadCallback _rewardedAdLoadCallback = new RewardedAdLoadCallback() {
		@Override
		public void onRewardedAdLoaded() {
			_adListenerWrapper.onAdLoaded();
			if (_loadListener != null) {
				_loadListener.onAdLoaded();
			}
		}

		@Override
		public void onRewardedAdFailedToLoad(LoadAdError adError) {
			_adListenerWrapper.onAdFailedToLoad(adError.getCode(), adError.toString());
		}
	};

	private RewardedAdCallback rewardedAdCallback = new RewardedAdCallback () {
		@Override
		public void onRewardedAdOpened() {
			_adListenerWrapper.onAdOpened();
		}

		@Override
		public void onRewardedAdFailedToShow(AdError adError) {
			_adListenerWrapper.onAdFailedToShow(adError.getCode(), adError.toString());
		}

		@Override
		public void onUserEarnedReward(RewardItem rewardItem) {
			_adListenerWrapper.onUserEarnedReward();
		}

		@Override
		public void onRewardedAdClosed() {
			_adListenerWrapper.onAdClosed();
		}
	};

	public RewardedAdCallback getRewardedAdCallback() {
		return rewardedAdCallback;
	}

	public RewardedAdLoadCallback getRewardedAdLoadCallback() { return _rewardedAdLoadCallback; }

	public void setLoadListener(IScarLoadListener loadListener) {
		_loadListener = loadListener;
	}
}