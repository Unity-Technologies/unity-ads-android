package com.unity3d.scar.adapter.v2000.scarads;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.AdInfo;
import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.scarads.IScarAd;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ScarAdBase<T> implements IScarAd {

	protected T _adObj;
	protected Context _context;
	protected ScarAdMetadata _scarAdMetadata;
	protected QueryInfo _queryInfo;
	protected ScarAdListener _scarAdListener;
	protected IAdsErrorHandler _adsErrorHandler;

	public ScarAdBase(Context context, ScarAdMetadata scarAdMetadata, QueryInfo queryInfo, IAdsErrorHandler adsErrorHandler) {
		_context = context;
		_scarAdMetadata = scarAdMetadata;
		_queryInfo = queryInfo;
		_adsErrorHandler = adsErrorHandler;
	}

	public void setGmaAd(T rewardedAd) {
		_adObj = rewardedAd;
	}

	@Override
	public void loadAd(IScarLoadListener loadListener) {
		if (_queryInfo != null) {
			AdInfo adInfo = new AdInfo(_queryInfo, _scarAdMetadata.getAdString());
			AdRequest adRequest = new AdRequest.Builder().setAdInfo(adInfo).build();
			_scarAdListener.setLoadListener(loadListener);
			loadAdInternal(adRequest, loadListener);
		} else {
			_adsErrorHandler.handleError(GMAAdsError.QueryNotFoundError(_scarAdMetadata));
		}
	}

	protected abstract void loadAdInternal(AdRequest adRequest, IScarLoadListener loadListener);
}
