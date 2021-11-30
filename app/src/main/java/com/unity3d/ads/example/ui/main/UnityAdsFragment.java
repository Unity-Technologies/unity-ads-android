package com.unity3d.ads.example.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.example.R;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.core.webview.WebView;

/**
 * A placeholder fragment containing a simple view.
 */
public class UnityAdsFragment extends Fragment {

	private final String LOGTAG = "UnityAdsExample";

	private View root;
	private EditText gameIdEdit;
	private CheckBox testModeCheckBox;
	private Button initializeButton;
	private Button loadInterstitialButton;
	private Button showInterstitialButton;
	private Button loadRewardedButton;
	private Button showRewardedButton;
	private Button showBannerButton;
	private Button hideBannerButton;
	private BannerView bottomBanner;
	private RelativeLayout bannerLayout;

	// Listener for banner events
	private BannerView.IListener bannerListener = new BannerView.IListener() {
		@Override
		public void onBannerLoaded(BannerView bannerAdView) {
			Log.v(LOGTAG, "onBannerLoaded: " + bannerAdView.getPlacementId());
			enableButton(hideBannerButton);
		}

		@Override
		public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
			Log.e(LOGTAG, "Unity Ads failed to load banner for " + bannerAdView.getPlacementId() + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage);
		}

		@Override
		public void onBannerClick(BannerView bannerAdView) {
			Log.v(LOGTAG, "onBannerClick: " + bannerAdView.getPlacementId());
		}

		@Override
		public void onBannerLeftApplication(BannerView bannerAdView) {
			Log.v(LOGTAG, "onBannerLeftApplication: " + bannerAdView.getPlacementId());
		}
	};


	private final String interstitialPlacementId = "video";
	private final String rewardedPlacementId = "rewardedVideo";

	private int ordinal = 1;
	final private String defaultGameId = "14851";

	public static UnityAdsFragment newInstance(int index) {
		UnityAdsFragment fragment = new UnityAdsFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		this.root = inflater.inflate(R.layout.fragment_unity_ads, container, false);
		this.gameIdEdit = root.findViewById(R.id.unityads_example_gameid_edit);
		this.testModeCheckBox = root.findViewById(R.id.unityads_example_testmode_checkbox);
		this.initializeButton = root.findViewById(R.id.unityads_example_initialize_button);
		this.loadInterstitialButton = root.findViewById(R.id.unityads_example_load_interstitial_button);
		this.showInterstitialButton = root.findViewById(R.id.unityads_example_show_interstitial_button);
		this.loadRewardedButton = root.findViewById(R.id.unityads_example_load_rewarded_button);
		this.showRewardedButton = root.findViewById(R.id.unityads_example_show_rewarded_button);
		this.showBannerButton = root.findViewById(R.id.unityads_example_show_banner_button);
		this.hideBannerButton = root.findViewById(R.id.unityads_example_hide_banner_button);
		this.bannerLayout = root.findViewById(R.id.unityads_example_banner_layout);

		enableButton(initializeButton);
		disableButton(loadInterstitialButton);
		disableButton(showInterstitialButton);
		disableButton(loadRewardedButton);
		disableButton(showRewardedButton);
		disableButton(showBannerButton);
		disableButton(hideBannerButton);

		SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
		gameIdEdit.setText(preferences.getString("gameId", defaultGameId));
		testModeCheckBox.setChecked( true);

		if(Build.VERSION.SDK_INT >= 19) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		UnityAds.setDebugMode(true);

		this.initializeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String gameId = gameIdEdit.getText().toString();
				if (gameId.isEmpty()) {
					Toast.makeText(getActivity().getApplicationContext(), "Missing Game ID", Toast.LENGTH_SHORT).show();
					return;
				}

				disableButton(initializeButton);
				gameIdEdit.setEnabled(false);
				testModeCheckBox.setEnabled(false);

				UnityAds.initialize(getContext(), gameId, testModeCheckBox.isChecked(), new IUnityAdsInitializationListener() {
					@Override
					public void onInitializationComplete() {
						Log.v(LOGTAG, "Unity Ads initialization complete");
						enableButton(loadInterstitialButton);
						enableButton(loadRewardedButton);
						enableButton(showBannerButton);
					}

					@Override
					public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
						Log.e(LOGTAG, "Unity Ads initialization failed: [" + error + "] " + message);
					}
				});

				// Store entered Game ID in App Settings
				SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
				SharedPreferences.Editor preferencesEdit = preferences.edit();
				preferencesEdit.putString("gameId", gameId);
				preferencesEdit.commit();
			}
		});

		this.loadInterstitialButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(loadInterstitialButton);
				loadAd(true);
			}
		});

		this.loadRewardedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(loadRewardedButton);
				loadAd(false);
			}
		});

		this.showInterstitialButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(showInterstitialButton);
				showAd(true);
			}
		});

		this.showRewardedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(showRewardedButton);
				showAd(false);
			}
		});

		this.showBannerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomBanner = new BannerView((Activity)v.getContext(), "bannerads", new UnityBannerSize(320, 50));
				bottomBanner.setListener(bannerListener);
				bottomBanner.load();
				bannerLayout.addView(bottomBanner);
			}
		});

		hideBannerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomBanner.removeAllViews();
				bottomBanner = null;
				showBannerButton.setEnabled(true);
			}
		});

		return root;
	}

	private void loadAd(boolean canSkip) {
		String placementToLoad = canSkip ? interstitialPlacementId : rewardedPlacementId;
		PlayerMetaData playerMetaData = new PlayerMetaData(getActivity());
		playerMetaData.setServerId("rikshot");
		playerMetaData.commit();

		MediationMetaData ordinalMetaData = new MediationMetaData(getActivity());
		ordinalMetaData.setOrdinal(ordinal++);
		ordinalMetaData.commit();

		Log.v(LOGTAG, "Loading ad for " + placementToLoad + "...");

		UnityAds.load(placementToLoad, new IUnityAdsLoadListener() {
			@Override
			public void onUnityAdsAdLoaded(String placementId) {
				Log.v(LOGTAG, "Ad for " + placementId + " loaded");
				enableButton(canSkip ? showInterstitialButton : showRewardedButton);
			}

			@Override
			public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
				Log.e(LOGTAG, "Ad for " + placementId + " failed to load: [" + error + "] " + message);
				enableButton(canSkip ? loadInterstitialButton : loadRewardedButton);
			}
		});
	}

	private void showAd(boolean canSkip) {
		String placementToShow = canSkip ? interstitialPlacementId : rewardedPlacementId;

		UnityAds.show(getActivity(), placementToShow, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
			@Override
			public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
				Log.e(LOGTAG, "onUnityAdsShowFailure: " + error + " - " + message);
			}

			@Override
			public void onUnityAdsShowStart(String placementId) {
				Log.v(LOGTAG, "onUnityAdsShowStart: " + placementId);
			}

			@Override
			public void onUnityAdsShowClick(String placementId) {
				Log.v(LOGTAG,"onUnityAdsShowClick: " + placementId);
			}

			@Override
			public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
				Log.v(LOGTAG,"onUnityAdsShowComplete: " + placementId);
				enableButton(canSkip ? loadInterstitialButton : loadRewardedButton);
			}
		});
	}


	private void enableButton (Button btn) {
		btn.setEnabled(true);
		float alpha = 1f;
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		btn.startAnimation(alphaUp);
	}

	private void disableButton (Button btn) {
		float alpha = 0.45f;
		btn.setEnabled(false);
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		btn.startAnimation(alphaUp);
	}
}
