package com.unity3d.scar.adapter.v1920.signals;

import android.util.Log;

import com.google.android.gms.ads.query.QueryInfo;
import com.google.android.gms.ads.query.QueryInfoGenerationCallback;
import com.unity3d.scar.adapter.common.DispatchGroup;
import com.unity3d.scar.adapter.v1920.ScarAdapter;

public class QueryInfoCallback extends QueryInfoGenerationCallback {

	private DispatchGroup _dispatchGroup;
	private QueryInfoMetadata _gmaQueryInfoMetadata;

	public QueryInfoCallback(final QueryInfoMetadata gmaQueryInfoMetadata, final DispatchGroup dispatchGroup) {
		_dispatchGroup = dispatchGroup;
		_gmaQueryInfoMetadata = gmaQueryInfoMetadata;
	}

	// Called when QueryInfo generation succeeds
	@Override
	public void onSuccess(final QueryInfo queryInfo) {
		_gmaQueryInfoMetadata.setQueryInfo(queryInfo);
		_dispatchGroup.leave();
	}

	// Called when QueryInfo generation fails
	@Override
	public void onFailure(String failureMsg) {
		_gmaQueryInfoMetadata.setError(failureMsg);
		_dispatchGroup.leave();
	}
}
