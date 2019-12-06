package com.unity3d.ads.example.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.example.R;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.view.BannerPosition;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * A placeholder fragment containing a simple view.
 */
public class UnityAdsFragment extends Fragment implements IUnityAdsListener, IUnityBannerListener {

	private View root;
	private EditText gameIdEdit;
	private CheckBox testModeCheckBox;
	private Button initializeButton;
	private Button interstitialButton;
	private Button incentivizedButton;
	private Button bannerButton;
	private TextView statusText;
	private boolean bannerShowing = false;

	private String interstitialPlacementId;
	private String incentivizedPlacementId;

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
		this.interstitialButton = root.findViewById(R.id.unityads_example_interstitial_button);
		this.incentivizedButton = root.findViewById(R.id.unityads_example_incentivized_button);
		this.bannerButton = root.findViewById(R.id.unityads_example_banner_button);
		this.statusText = root.findViewById(R.id.unityads_example_statustext);

		enableButton(initializeButton);
		disableButton(interstitialButton);
		disableButton(incentivizedButton);
		enableButton(bannerButton);

		SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
		gameIdEdit.setText(preferences.getString("gameId", defaultGameId));
		testModeCheckBox.setChecked( true);

		if(Build.VERSION.SDK_INT >= 19) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		UnityAds.addListener(this);
		UnityAds.setDebugMode(true);

		MediationMetaData mediationMetaData = new MediationMetaData(getActivity());
		mediationMetaData.setName("mediationPartner");
		mediationMetaData.setVersion("v12345");
		mediationMetaData.commit();

		MetaData debugMetaData = new MetaData(getActivity());
		debugMetaData.set("test.debugOverlayEnabled", true);
		debugMetaData.commit();

		this.initializeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String gameId = gameIdEdit.getText().toString();
				if (gameId.isEmpty()) {
					Toast.makeText(getActivity().getApplicationContext(), "Missing game id", Toast.LENGTH_SHORT).show();
					return;
				}

				disableButton(initializeButton);
				gameIdEdit.setEnabled(false);
				testModeCheckBox.setEnabled(false);

				statusText.setText("Initializing...");
				UnityAds.addListener(UnityAdsFragment.this);
				UnityAds.initialize(getActivity(), gameId, testModeCheckBox.isChecked());

				// store entered gameid in app settings
				SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
				SharedPreferences.Editor preferencesEdit = preferences.edit();
				preferencesEdit.putString("gameId", gameId);
				preferencesEdit.commit();
			}
		});

		this.interstitialButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(interstitialButton);

				PlayerMetaData playerMetaData = new PlayerMetaData(getActivity());
				playerMetaData.setServerId("rikshot");
				playerMetaData.commit();

				MediationMetaData ordinalMetaData = new MediationMetaData(getActivity());
				ordinalMetaData.setOrdinal(ordinal++);
				ordinalMetaData.commit();

				UnityAds.show(getActivity(), interstitialPlacementId);
			}
		});

		this.incentivizedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(incentivizedButton);

				PlayerMetaData playerMetaData = new PlayerMetaData(getActivity());
				playerMetaData.setServerId("rikshot");
				playerMetaData.commit();

				MediationMetaData ordinalMetaData = new MediationMetaData(getActivity());
				ordinalMetaData.setOrdinal(ordinal++);
				ordinalMetaData.commit();

				UnityAds.show(getActivity(), incentivizedPlacementId);
			}
		});

		this.bannerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (UnityAdsFragment.this.bannerShowing) {
					UnityBanners.destroy();
					UnityAdsFragment.this.bannerButton.setText(R.string.show_banner);
					UnityAdsFragment.this.bannerShowing = false;
				} else {
					UnityAdsFragment.this.bannerShowing = true;
					UnityAdsFragment.this.bannerButton.setText(R.string.hide_banner);
					UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
					UnityBanners.setBannerListener(UnityAdsFragment.this);
					UnityBanners.loadBanner(getActivity(), "bannerads");
				}
			}
		});


		return root;
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

	/* LISTENER */
	@Override
	public void onUnityAdsReady(final String zoneId) {
		TextView statusText = this.statusText;
		final Button interstitialButton = this.interstitialButton;
		final Button incentivizedButton = this.incentivizedButton;
		statusText.setText("");

		DeviceLog.debug("onUnityAdsReady: " + zoneId);
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// look for various default placement ids over time
				switch (zoneId) {
					case "video":
					case "defaultZone":
					case "defaultVideoAndPictureZone":
						interstitialPlacementId = zoneId;
						enableButton(interstitialButton);
						break;

					case "rewardedVideo":
					case "rewardedVideoZone":
					case "incentivizedZone":
						incentivizedPlacementId = zoneId;
						enableButton(incentivizedButton);
						break;
				}
			}
		});

		toast("Ready", zoneId);
	}

	@Override
	public void onUnityAdsStart(String zoneId) {
		DeviceLog.debug("onUnityAdsStart: " + zoneId);
		toast("Start", zoneId);
	}

	@Override
	public void onUnityAdsFinish(String zoneId, UnityAds.FinishState result) {
		DeviceLog.debug("onUnityAdsFinish: " + zoneId + " - " + result);
		toast("Finish", zoneId + " " + result);
	}

	@Override
	public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
		DeviceLog.debug("onUnityAdsError: " + error + " - " + message);
		toast("Error", error + " " + message);

		TextView statusText = this.statusText;
		statusText.setText(error + " - " + message);
	}

	private void toast(String callback, String msg) {
		if (getContext() != null) {
			Toast.makeText(getContext(), callback + ": " + msg, Toast.LENGTH_SHORT).show();
		}
	}

	public void onUnityBannerLoaded(String placementId, final View view) {
		final UnityAdsFragment self = this;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (view.getParent() == null) {
					self.getActivity().addContentView(view, view.getLayoutParams());
				}
			}
		});
	}

	public void onUnityBannerUnloaded(String placementId) {

	}

	public void onUnityBannerShow(String placementId) {

	}

	public void onUnityBannerClick(String placementId) {

	}

	public void onUnityBannerHide(String placementId) {

	}

	public void onUnityBannerError(String message) {
		Log.e("BANNER ERROR", message);
	}
}
