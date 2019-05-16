package com.unity3d.ads.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.PlayerMetaData;

import com.unity3d.services.UnityServices;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.view.BannerPosition;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.monetization.IUnityMonetizationListener;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdListenerAdapter;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdPlacementContent;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.monetization.placementcontent.purchasing.PromoAdPlacementContent;
import com.unity3d.services.monetization.placementcontent.purchasing.NativePromoAdapter;
import com.unity3d.services.monetization.placementcontent.purchasing.PromoMetadata;
import com.unity3d.services.purchasing.UnityPurchasing;
import com.unity3d.services.purchasing.core.IPurchasingAdapter;
import com.unity3d.services.purchasing.core.IRetrieveProductsListener;
import com.unity3d.services.purchasing.core.ITransactionListener;
import com.unity3d.services.purchasing.core.Product;
import com.unity3d.services.purchasing.core.TransactionDetails;

import java.util.Arrays;
import java.util.Map;

public class UnityMonetizationExample extends Activity {

	final private String defaultGameId = "14851";

	private String interstitialPlacementId;
	private String incentivizedPlacementId;
	private String bannerPlacementId = "bannerads";

	private static int ordinal = 1;

	private Button interstitialButton;
	private Button incentivizedButton;
	private Button bannerButton;

	private View bannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unityads_example_layout);
		final UnityMonetizationExample thisActivity = this;
		final IUnityMonetizationListener unityMonetizationListener = new UnityMonetizationListener();
		final IUnityBannerListener unityBannerListener = new UnityBannerListener();

		if(Build.VERSION.SDK_INT >= 19) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		UnityMonetization.setListener(unityMonetizationListener);
		UnityPurchasing.setAdapter(new UnityPurchasingAdapter());

		UnityBanners.setBannerListener(unityBannerListener);

		MediationMetaData mediationMetaData = new MediationMetaData(this);
		mediationMetaData.setName("mediationPartner");
		mediationMetaData.setVersion("v12345");
		mediationMetaData.commit();

		interstitialButton = (Button) findViewById(R.id.unityads_example_interstitial_button);
		disableButton(interstitialButton);
		interstitialButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(interstitialButton);

				PlayerMetaData playerMetaData = new PlayerMetaData(thisActivity);
				playerMetaData.setServerId("rikshot");
				playerMetaData.commit();

				MediationMetaData ordinalMetaData = new MediationMetaData(thisActivity);
				ordinalMetaData.setOrdinal(ordinal++);
				ordinalMetaData.commit();

				PlacementContent placementContent = UnityMonetization.getPlacementContent(interstitialPlacementId);
				if (placementContent instanceof PromoAdPlacementContent) {
					showPromo((PromoAdPlacementContent) placementContent);
				} else if (placementContent instanceof ShowAdPlacementContent) {
					((ShowAdPlacementContent)placementContent).show(thisActivity, new ShowAdListenerAdapter() {
						@Override
						public void onAdStarted(String placementId) {
							toast("Start", interstitialPlacementId);
						}

						@Override
						public void onAdFinished(String placementId, UnityAds.FinishState withState) {
							DeviceLog.debug("onUnityAdsFinish: " + interstitialPlacementId + " - " + withState);
							toast("Finish", interstitialPlacementId + " " + withState);
						}
					});
				} 
			}
		});

		incentivizedButton = (Button) findViewById(R.id.unityads_example_incentivized_button);
		disableButton(incentivizedButton);
		incentivizedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton(incentivizedButton);

				PlayerMetaData playerMetaData = new PlayerMetaData(thisActivity);
				playerMetaData.setServerId("rikshot");
				playerMetaData.commit();

				MediationMetaData ordinalMetaData = new MediationMetaData(thisActivity);
				ordinalMetaData.setOrdinal(ordinal++);
				ordinalMetaData.commit();

				PlacementContent placementContent = UnityMonetization.getPlacementContent(incentivizedPlacementId);
				if (placementContent instanceof PromoAdPlacementContent) {
					showPromo((PromoAdPlacementContent) placementContent);
				} else if (placementContent instanceof ShowAdPlacementContent) {
					((ShowAdPlacementContent)placementContent).show(thisActivity, new ShowAdListenerAdapter() {
                        @Override
						public void onAdStarted(String placementId) {
							toast("Start", incentivizedPlacementId);
						}

						@Override
						public void onAdFinished(String placementId, UnityAds.FinishState withState) {
							DeviceLog.debug("onUnityAdsFinish: " + incentivizedPlacementId + " - " + withState);
							toast("Finish", incentivizedPlacementId + " " + withState);
						}
					});
				}
			}
		});

		bannerButton = (Button) findViewById(R.id.unityads_example_banner_button);
		disableButton(bannerButton);
		bannerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (bannerView == null) {
					UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
					UnityBanners.loadBanner(thisActivity, bannerPlacementId);
				} else {
					UnityBanners.destroy();
				}
			}
		});

		final Button initializeButton = (Button) findViewById(R.id.unityads_example_initialize_button);
		final EditText gameIdEdit = (EditText) findViewById(R.id.unityads_example_gameid_edit);
		final CheckBox testModeCheckbox = (CheckBox) findViewById(R.id.unityads_example_testmode_checkbox);

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

				toast("initializeButton onClick", "UnityServices Initializing");

				UnityMonetization.initialize(thisActivity, gameId, unityMonetizationListener,testModeCheckbox.isChecked());
				// store entered gameid in app settings
				SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
				SharedPreferences.Editor preferencesEdit = preferences.edit();
				preferencesEdit.putString("gameId", gameId);
				preferencesEdit.commit();

				enableButton(bannerButton);
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

			if (UnityMonetization.isReady(interstitialPlacementId)) {
				enableButton((Button) findViewById(R.id.unityads_example_interstitial_button));
			}
			else {
				disableButton((Button) findViewById(R.id.unityads_example_interstitial_button));
			}

			if (UnityMonetization.isReady(incentivizedPlacementId)) {
				enableButton((Button) findViewById(R.id.unityads_example_incentivized_button));
			}
			else {
				disableButton((Button) findViewById(R.id.unityads_example_incentivized_button));
			}

			enableButton(bannerButton);
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

	private class UnityBannerListener implements IUnityBannerListener {

		@Override
		public void onUnityBannerLoaded(String placementId, View view) {
			bannerView = view;
			((ViewGroup)findViewById(R.id.unityads_example_layout_root)).addView(view);
			toast("onUnityBannerLoaded", "Banner loaded");
		}

		@Override
		public void onUnityBannerUnloaded(String placementId) {
			toast("onUnityBannerUnloaded", placementId + " unloaded");
			bannerView = null;
		}

		@Override
		public void onUnityBannerShow(String placementId) {
			toast("onUnityBannerShow", placementId + " show");
		}

		@Override
		public void onUnityBannerClick(String placementId) {
			toast("onUnityBannerShow", placementId + " click");
		}

		@Override
		public void onUnityBannerHide(String placementId) {
			toast("onUnityBannerHide", placementId + " hide");
		}

		@Override
		public void onUnityBannerError(String message) {
			toast("onUnityBannerError", "Banner error: " + message);
		}
	}

	private class UnityMonetizationListener implements IUnityMonetizationListener {

		@Override
		public void onPlacementContentReady(String placementId, PlacementContent placementContent) {
			toast("onPlacementContentReady", "Initialized");

			// look for various default placement ids over time
			switch (placementId) {
				case "video":
				case "singlePlacement":
				case "mixedPlacement":
					interstitialPlacementId = placementId;
					enableButton(interstitialButton);
					if (placementContent instanceof PromoAdPlacementContent) {
						interstitialButton.setText("Promo");
					} else if (placementContent instanceof ShowAdPlacementContent) {
						ShowAdPlacementContent adPlacementContent = ((ShowAdPlacementContent)placementContent);
						String text = "Show Ad";
					    if (adPlacementContent.isRewarded()) {
					    	text = text + " (Rewarded)";
						}
						interstitialButton.setText(text);
					}
					break;
				case "rewardedVideoZone":
				case "incentivizedZone":
					incentivizedPlacementId = placementId;
					enableButton(incentivizedButton);
					if (placementContent instanceof ShowAdPlacementContent) {
						ShowAdPlacementContent adPlacementContent = ((ShowAdPlacementContent)placementContent);
						String text = "Show Ad";
						if (adPlacementContent.isRewarded()) {
							text = text + " (Rewarded)";
						}
						interstitialButton.setText(text);
					}
					break;
			}
		}

		@Override
		public void onPlacementContentStateChange(String placementId, PlacementContent placementContent, UnityMonetization.PlacementContentState previousState, UnityMonetization.PlacementContentState newState) {

		}

		@Override
		public void onUnityServicesError(UnityServices.UnityServicesError error, String message) {
			toast("Error", String.format("%s - %s", error, message));
		}
	}

    private void toast(String callback, String msg) {
        Toast.makeText(getApplicationContext(), callback + ": " + msg, Toast.LENGTH_SHORT).show();
    }

    private class UnityPurchasingAdapter implements IPurchasingAdapter {

		@Override
		public void retrieveProducts(IRetrieveProductsListener listener) {
			listener.onProductsRetrieved(Arrays.asList(Product.newBuilder()
					.withProductId("com.unity3d.monteization.example.productID")
					.withLocalizedTitle("productTitle")
					.withLocalizedPriceString("$1.99")
					.withProductType("PREMIUM")
					.withIsoCurrencyCode("USD")
					.withLocalizedPrice(1.99)
					.withLocalizedDescription("Localized Description")
					.build()));
		}

		@Override
		public void onPurchase(String productID, ITransactionListener listener, Map<String, Object> extras) {
			toast("Purchasing", "Wants to purchase " + productID);
			listener.onTransactionComplete(TransactionDetails.newBuilder()
					.withTransactionId("foobar")
					.withReceipt("What is a receipt even?")
					.putExtra("foo", "bar")
					.build());
		}
	}

	private void showPromo(final PromoAdPlacementContent placementContent) {
        final NativePromoAdapter nativePromoAdapter = new NativePromoAdapter(placementContent);
		PromoMetadata metadata = placementContent.getMetadata();
		Product product = metadata.getPremiumProduct();
		String price = product == null ? "$0.99" : product.getLocalizedPriceString();
		final View root = getLayoutInflater().inflate(R.layout.unitymonetization_native_promo, (ViewGroup) findViewById(R.id.unityads_example_layout_root));
		Button buyButton = root.findViewById(R.id.native_promo_buy_button);
		Button closeButton = root.findViewById(R.id.native_promo_close_button);
		buyButton.setText("Buy now for only " + price + "!");

		nativePromoAdapter.onShown();
		buyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Do purchase then call
				nativePromoAdapter.onClicked();
				nativePromoAdapter.onClosed();
				((ViewGroup)root).removeView(findViewById(R.id.native_promo_root));


			}
		});
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nativePromoAdapter.onClosed();
				((ViewGroup)root).removeView(findViewById(R.id.native_promo_root));
			}
		});
	}
}
