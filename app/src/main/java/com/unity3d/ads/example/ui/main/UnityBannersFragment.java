package com.unity3d.ads.example.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import com.unity3d.ads.example.R;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.view.BannerPosition;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.misc.ViewUtilities;

public class UnityBannersFragment extends Fragment implements IUnityBannerListener {

	private Button topBannerButton;
	private Button unityBannerAdOldButton;
	private LinearLayout topBannerContainer;
	private BannerView topBannerAdView;
	private BannerView topBannerView;
	private boolean unityBannerOldShowing = false;
	private BannerView.IListener bannerAdViewListener;

	public static UnityBannersFragment newInstance(int index) {
		UnityBannersFragment fragment = new UnityBannersFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.bannerAdViewListener = this.createBannerListener();
	}

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_unity_banners, container, false);
		this.topBannerButton = root.findViewById(R.id.top_banner_button);
		this.unityBannerAdOldButton = root.findViewById(R.id.unity_banner_ad_old_button);
		this.topBannerContainer = root.findViewById(R.id.top_banner_container);

		this.enableButton(topBannerButton);
		this.enableButton(unityBannerAdOldButton);

		final UnityBannersFragment self = this;
		this.topBannerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (self.topBannerView != null) {
					self.topBannerContainer.removeView(self.topBannerView);
					self.topBannerView.destroy();
					self.topBannerView = null;
					self.topBannerButton.setText(R.string.show_top_banner);
				} else {
					int widthInDp = Math.round(ViewUtilities.dpFromPx(self.getContext(), self.topBannerContainer.getWidth()));
					int heightInDp = Math.round(ViewUtilities.dpFromPx(self.getContext(), self.topBannerContainer.getHeight()));
					UnityBannerSize unityBannerSize = new UnityBannerSize(widthInDp, heightInDp);
					self.topBannerView = new BannerView(self.getActivity(), "bannerads", unityBannerSize);
					self.topBannerView.setListener(self.bannerAdViewListener);
					self.topBannerView.load();
					self.topBannerButton.setText(R.string.hide_top_banner);
					self.topBannerContainer.addView(self.topBannerView);
				}
			}
		});

		this.unityBannerAdOldButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (self.unityBannerOldShowing) {
					UnityBanners.destroy();
					self.unityBannerAdOldButton.setText(R.string.show_old_banner);
					self.unityBannerOldShowing = false;
				} else {
					self.unityBannerOldShowing = true;
					self.unityBannerAdOldButton.setText(R.string.hide_old_banner);
					UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
					UnityBanners.setBannerListener(self);
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

	private BannerView.IListener createBannerListener() {
		final UnityBannersFragment self = this;
		return new BannerView.Listener() {
			@Override
			public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
				if (self.topBannerView != null && self.topBannerView == bannerAdView) {
					self.topBannerContainer.removeView(self.topBannerView);
					self.topBannerView.destroy();
					self.topBannerView = null;
					self.topBannerButton.setText(R.string.show_top_banner);
				}
			}
			@Override
            public void onBannerLoaded(BannerView bannerAdView) {
				Log.d("UnityAdsExample", "onBannerLoded is called for: " + bannerAdView.getPlacementId());
            }

            @Override
            public void onBannerClick(BannerView bannerAdView) {
				Log.d("UnityAdsExample", "onBannerClick is called for: " + bannerAdView.getPlacementId());
            }

            @Override
            public void onBannerLeftApplication(BannerView bannerAdView) {
				Log.d("UnityAdsExample", "onBannerLeftApplication is called for: " + bannerAdView.getPlacementId());
            }
		};
	}

	public void onUnityBannerLoaded(String placementId, final View view) {
		final UnityBannersFragment self = this;
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
