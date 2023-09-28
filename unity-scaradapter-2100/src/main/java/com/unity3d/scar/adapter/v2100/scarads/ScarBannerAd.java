package com.unity3d.scar.adapter.v2100.scarads;

import android.content.Context;
import android.widget.RelativeLayout;
import com.google.android.gms.ads.*;
import com.unity3d.scar.adapter.common.*;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v2100.requests.AdRequestFactory;

public class ScarBannerAd extends ScarAdBase<AdView>{

	private RelativeLayout _bannerView;
	private int _width;
	private int _height;
	private AdView _adView;

	public ScarBannerAd(Context context, RelativeLayout bannerView, AdRequestFactory adRequestFactory, ScarAdMetadata scarAdMetadata, int width, int height, IAdsErrorHandler adsErrorHandler, IScarBannerAdListenerWrapper adListener) {
		super(context, scarAdMetadata, adRequestFactory, adsErrorHandler);
		_bannerView = bannerView;
		_width = width;
		_height = height;
		_adView = new AdView(_context);
		_scarAdListener = new ScarBannerAdListener(adListener, this);
	}

	public void removeAdView() {
		if (_bannerView != null && _adView != null) {
			_bannerView.removeView(_adView);
		}
	}

	@Override
	protected void loadAdInternal(AdRequest adRequest, IScarLoadListener loadListener) {
		if (_bannerView != null && _adView != null) {
			_bannerView.addView(_adView);

			_adView.setAdSize(new AdSize(_width, _height));
			_adView.setAdUnitId(_scarAdMetadata.getAdUnitId());

			_adView.setAdListener(((ScarBannerAdListener)_scarAdListener).getAdListener());

			_adView.loadAd(adRequest);
		}
	}
}
