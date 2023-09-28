package com.unity3d.scar.adapter.v2100.scarads;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.scarads.IScarAd;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v2100.requests.AdRequestFactory;

public abstract class ScarAdBase<T> implements IScarAd {

	protected T _adObj;
	protected Context _context;
	protected ScarAdMetadata _scarAdMetadata;
	protected AdRequestFactory _adRequestFactory;
	protected ScarAdListener _scarAdListener;
	protected IAdsErrorHandler _adsErrorHandler;

	public ScarAdBase(Context context, ScarAdMetadata scarAdMetadata, AdRequestFactory adRequestFactory, IAdsErrorHandler adsErrorHandler) {
		_context = context;
		_scarAdMetadata = scarAdMetadata;
		_adRequestFactory = adRequestFactory;
		_adsErrorHandler = adsErrorHandler;
	}

	public void setGmaAd(T rewardedAd) {
		_adObj = rewardedAd;
	}

	@Override
	public void loadAd(IScarLoadListener loadListener) {
		AdRequest adRequest = _adRequestFactory.buildAdRequestWithAdString(_scarAdMetadata.getAdString());
		if (loadListener != null) {
			_scarAdListener.setLoadListener(loadListener);
		}
		loadAdInternal(adRequest, loadListener);
	}

	protected abstract void loadAdInternal(AdRequest adRequest, IScarLoadListener loadListener);
}

