package com.unity3d.scar.adapter.v1950.scarads;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.AdInfo;
import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.scarads.IScarAd;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;

public abstract class ScarAdBase implements IScarAd {

	protected Context _context;
	protected ScarAdMetadata _scarAdMetadata;
	protected QueryInfo _queryInfo;
	protected IAdsErrorHandler _adsErrorHandler;

	public ScarAdBase(Context context, ScarAdMetadata scarAdMetadata, QueryInfo queryInfo, IAdsErrorHandler adsErrorHandler) {
		_context = context;
		_scarAdMetadata = scarAdMetadata;
		_queryInfo = queryInfo;
		_adsErrorHandler = adsErrorHandler;
	}

	@Override
	public void loadAd(IScarLoadListener loadListener) {
		if (_queryInfo != null) {
			AdInfo adInfo = new AdInfo(_queryInfo, _scarAdMetadata.getAdString());
			AdRequest adRequest = new AdRequest.Builder().setAdInfo(adInfo).build();
			loadAdInternal(loadListener, adRequest);
		} else {
			_adsErrorHandler.handleError(GMAAdsError.QueryNotFoundError(_scarAdMetadata));
		}
	}

	protected abstract void loadAdInternal(IScarLoadListener loadListener, AdRequest adRequest);

}
