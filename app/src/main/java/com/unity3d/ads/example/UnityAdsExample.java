package com.unity3d.ads.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.WebView;

public class UnityAdsExample extends Activity {

	final private String defaultGameId = "14851";

	private String interstitialPlacementId;
	private String incentivizedPlacementId;

	private static int ordinal = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unityads_example_layout);
		final UnityAdsExample self = this;
		final UnityAdsListener unityAdsListener = new UnityAdsListener();

		if(Build.VERSION.SDK_INT >= 19) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		UnityAds.setListener(unityAdsListener);
		UnityAds.setDebugMode(true);

		MediationMetaData mediationMetaData = new MediationMetaData(this);
		mediationMetaData.setName("mediationPartner");
		mediationMetaData.setVersion("v12345");
		mediationMetaData.commit();

		MetaData debugMetaData = new MetaData(this);
		debugMetaData.set("test.debugOverlayEnabled", true);
		debugMetaData.commit();

		final Button interstitialButton = (Button) findViewById(R.id.unityads_example_interstitial_button);
		disableButton(interstitialButton);
		interstitialButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(interstitialButton);

				PlayerMetaData playerMetaData = new PlayerMetaData(self);
				playerMetaData.setServerId("rikshot");
				playerMetaData.commit();

				MediationMetaData ordinalMetaData = new MediationMetaData(self);
				ordinalMetaData.setOrdinal(ordinal++);
				ordinalMetaData.commit();

				UnityAds.show(self, interstitialPlacementId);
			}
		});

		final Button incentivizedButton = (Button) findViewById(R.id.unityads_example_incentivized_button);
		disableButton(incentivizedButton);
		incentivizedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(incentivizedButton);

				PlayerMetaData playerMetaData = new PlayerMetaData(self);
				playerMetaData.setServerId("rikshot");
				playerMetaData.commit();

				MediationMetaData ordinalMetaData = new MediationMetaData(self);
				ordinalMetaData.setOrdinal(ordinal++);
				ordinalMetaData.commit();

				UnityAds.show(self, incentivizedPlacementId);
			}
		});

		final Button initializeButton = (Button) findViewById(R.id.unityads_example_initialize_button);
		final EditText gameIdEdit = (EditText) findViewById(R.id.unityads_example_gameid_edit);
		final CheckBox testModeCheckbox = (CheckBox) findViewById(R.id.unityads_example_testmode_checkbox);
		final TextView statusText = (TextView) findViewById(R.id.unityads_example_statustext);

		SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
		gameIdEdit.setText(preferences.getString("gameId", defaultGameId));
               testModeCheckbox.setChecked(true);

		initializeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String gameId = gameIdEdit.getText().toString();
				if (gameId.isEmpty()) {
					Toast.makeText(getApplicationContext(), "Missing game id", Toast.LENGTH_SHORT).show();
					return;
				}

				disableButton(initializeButton);
				gameIdEdit.setEnabled(false);
				testModeCheckbox.setEnabled(false);

				statusText.setText("Initializing...");
				UnityAds.initialize(self, gameId, unityAdsListener, testModeCheckbox.isChecked());

				// store entered gameid in app settings
				SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
				SharedPreferences.Editor preferencesEdit = preferences.edit();
				preferencesEdit.putString("gameId", gameId);
				preferencesEdit.commit();
			}
		});

		LinearLayout layout = (LinearLayout)findViewById(R.id.unityads_example_button_container);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			layout.setOrientation(LinearLayout.HORIZONTAL);

		}
		else {
			layout.setOrientation(LinearLayout.VERTICAL);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (SdkProperties.isInitialized()) {
			disableButton((Button) findViewById(R.id.unityads_example_initialize_button));

			if (UnityAds.isReady(interstitialPlacementId)) {
				enableButton((Button) findViewById(R.id.unityads_example_interstitial_button));
			}
			else {
				disableButton((Button) findViewById(R.id.unityads_example_interstitial_button));
			}

			if (UnityAds.isReady(incentivizedPlacementId)) {
				enableButton((Button) findViewById(R.id.unityads_example_incentivized_button));
			}
			else {
				disableButton((Button) findViewById(R.id.unityads_example_incentivized_button));
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LinearLayout layout = (LinearLayout)findViewById(R.id.unityads_example_button_container);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			layout.setOrientation(LinearLayout.HORIZONTAL);

		}
		else {
			layout.setOrientation(LinearLayout.VERTICAL);
		}
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

	private class UnityAdsListener implements IUnityAdsListener {

		@Override
		public void onUnityAdsReady(final String zoneId) {
			TextView statusText = (TextView) findViewById(R.id.unityads_example_statustext);
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
							enableButton((Button) findViewById(R.id.unityads_example_interstitial_button));
							break;

						case "rewardedVideo":
						case "rewardedVideoZone":
						case "incentivizedZone":
							incentivizedPlacementId = zoneId;
							enableButton((Button) findViewById(R.id.unityads_example_incentivized_button));
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

			TextView statusText = (TextView) findViewById(R.id.unityads_example_statustext);
			statusText.setText(error + " - " + message);
		}

		private void toast(String callback, String msg) {
			Toast.makeText(getApplicationContext(), callback + ": " + msg, Toast.LENGTH_SHORT).show();
		}
	}
}
