package com.unity3d.scar.adapter.v2000.signals;

import com.google.android.gms.ads.query.QueryInfo;
import com.google.android.gms.ads.query.QueryInfoGenerationCallback;
import com.unity3d.scar.adapter.common.signals.ISignalCallbackListener;

public class QueryInfoCallback extends QueryInfoGenerationCallback {

	private String _placementId;
	private ISignalCallbackListener _signalCallbackListener;

	public QueryInfoCallback(final String placementId, final ISignalCallbackListener signalCallbackListener) {
		_placementId = placementId;
		_signalCallbackListener = signalCallbackListener;
	}

	@Override
	public void onSuccess(final QueryInfo queryInfo) {
		_signalCallbackListener.onSuccess(_placementId, queryInfo.getQuery(), queryInfo);
	}

	@Override
	public void onFailure(String errorMessage) {
		_signalCallbackListener.onFailure(errorMessage);
	}
}
