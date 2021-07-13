package com.unity3d.scar.adapter.v1920.scarads;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.AdInfo;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.scarads.IScarAd;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v1920.signals.QueryInfoMetadata;

public abstract class ScarAdBase implements IScarAd {

	protected Context _context;
	protected ScarAdMetadata _scarAdMetadata;
	protected QueryInfoMetadata _queryInfoMetadata;
	protected IAdsErrorHandler _adsErrorHandler;

	public ScarAdBase(Context context, ScarAdMetadata scarAdMetadata, QueryInfoMetadata queryInfoMetadata, IAdsErrorHandler adsErrorHandler) {
		_context = context;
		_scarAdMetadata = scarAdMetadata;
		_queryInfoMetadata = queryInfoMetadata;
		_adsErrorHandler = adsErrorHandler;
	}

	@Override
	public void loadAd(IScarLoadListener loadListener) {
		if (_queryInfoMetadata != null) {
			AdInfo adInfo = new AdInfo(_queryInfoMetadata.getQueryInfo(), _scarAdMetadata.getAdString());
			AdRequest adRequest = new AdRequest.Builder().setAdInfo(adInfo).build();
			loadAdInternal(loadListener, adRequest);
		} else {
			_adsErrorHandler.handleError(GMAAdsError.InternalLoadError(_scarAdMetadata));
		}
	}

	protected abstract void loadAdInternal(IScarLoadListener loadListener, AdRequest adRequest);
}
