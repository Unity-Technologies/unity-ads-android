package com.unity3d.scar.adapter.v2000.scarads;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.AdInfo;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.scarads.IScarAd;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v2000.signals.QueryInfoMetadata;

public abstract class ScarAdBase<T> implements IScarAd {

	protected T _adObj;
	protected Context _context;
	protected ScarAdMetadata _scarAdMetadata;
	protected QueryInfoMetadata _queryInfoMetadata;
	protected ScarAdListener _scarAdListener;
	protected IAdsErrorHandler _adsErrorHandler;

	public ScarAdBase(Context context, ScarAdMetadata scarAdMetadata, QueryInfoMetadata queryInfoMetadata, IAdsErrorHandler adsErrorHandler) {
		_context = context;
		_scarAdMetadata = scarAdMetadata;
		_queryInfoMetadata = queryInfoMetadata;
		_adsErrorHandler = adsErrorHandler;
	}

	public void setGmaAd(T rewardedAd) {
		_adObj = rewardedAd;
	}

	@Override
	public void loadAd(IScarLoadListener loadListener) {
		if (_queryInfoMetadata != null) {
			AdInfo adInfo = new AdInfo(_queryInfoMetadata.getQueryInfo(), _scarAdMetadata.getAdString());
			AdRequest adRequest = new AdRequest.Builder().setAdInfo(adInfo).build();
			_scarAdListener.setLoadListener(loadListener);
			loadAdInternal(adRequest, loadListener);
		} else {
			_adsErrorHandler.handleError(GMAAdsError.InternalLoadError(_scarAdMetadata));
		}
	}

	protected abstract void loadAdInternal(AdRequest adRequest, IScarLoadListener loadListener);

}
