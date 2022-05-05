package com.unity3d.scar.adapter.v2000.scarads;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;

public class ScarRewardedAdListener extends ScarAdListener {

	private final ScarRewardedAd _scarRewardedAd;
	private final IScarRewardedAdListenerWrapper _adListenerWrapper;

	public ScarRewardedAdListener(IScarRewardedAdListenerWrapper adListenerWrapper, ScarRewardedAd scarRewardedAd) {
		_adListenerWrapper = adListenerWrapper;
		_scarRewardedAd = scarRewardedAd;
	}

	private final RewardedAdLoadCallback _adLoadCallback = new RewardedAdLoadCallback() {
		@Override
		public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
			super.onAdLoaded(rewardedAd);
			_adListenerWrapper.onAdLoaded();
			rewardedAd.setFullScreenContentCallback(_fullScreenContentCallback);
			_scarRewardedAd.setGmaAd(rewardedAd);
			if (_loadListener != null) {
				_loadListener.onAdLoaded();
			}
		}

		@Override
		public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
			super.onAdFailedToLoad(loadAdError);
			_adListenerWrapper.onAdFailedToLoad(loadAdError.getCode(), loadAdError.toString());
		}
	};

	private final OnUserEarnedRewardListener _onUserEarnedRewardListener = new OnUserEarnedRewardListener() {
		@Override
		public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
			_adListenerWrapper.onUserEarnedReward();
		}
	};

	private final FullScreenContentCallback _fullScreenContentCallback = new FullScreenContentCallback() {
		@Override
		public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
			super.onAdFailedToShowFullScreenContent(adError);
			_adListenerWrapper.onAdFailedToShow(adError.getCode(), adError.toString());
		}

		@Override
		public void onAdShowedFullScreenContent() {
			super.onAdShowedFullScreenContent();
			_adListenerWrapper.onAdOpened();
		}

		@Override
		public void onAdDismissedFullScreenContent() {
			super.onAdDismissedFullScreenContent();
			_adListenerWrapper.onAdClosed();
		}

		@Override
		public void onAdImpression() {
			super.onAdImpression();
			_adListenerWrapper.onAdImpression();
		}
	};

	public OnUserEarnedRewardListener getOnUserEarnedRewardListener() {
		return _onUserEarnedRewardListener;
	}

	public RewardedAdLoadCallback getAdLoadListener() {
		return _adLoadCallback;
	}

}