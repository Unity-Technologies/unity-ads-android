package com.unity3d.scar.adapter.v1920.scarads;

import com.google.android.gms.ads.rewarded.RewardedAd;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;

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
			_adListenerWrapper.onRewardedAdLoaded();
			if (_loadListener != null) {
				_loadListener.onAdLoaded();
			}
		}

		@Override
		public void onRewardedAdFailedToLoad(int adErrorCode) {
			_adListenerWrapper.onRewardedAdFailedToLoad(adErrorCode, "SCAR ad failed to show");
		}
	};

	private RewardedAdCallback rewardedAdCallback = new RewardedAdCallback () {
		@Override
		public void onRewardedAdOpened() {
			_adListenerWrapper.onRewardedAdOpened();
		}

		@Override
		public void onRewardedAdFailedToShow(int adErrorCode) {
			_adListenerWrapper.onRewardedAdFailedToShow(adErrorCode, "SCAR ad failed to show");
		}

		@Override
		public void onUserEarnedReward(RewardItem rewardItem) {
			_adListenerWrapper.onUserEarnedReward();
		}

		@Override
		public void onRewardedAdClosed() {
			_adListenerWrapper.onRewardedAdClosed();
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
