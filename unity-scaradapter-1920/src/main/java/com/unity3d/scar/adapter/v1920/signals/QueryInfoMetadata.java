package com.unity3d.scar.adapter.v1920.signals;

import com.google.android.gms.ads.query.QueryInfo;

public class QueryInfoMetadata {
	private String _placementId;
	private QueryInfo _queryInfo;
	private String _error;

	public QueryInfoMetadata(String placementId) {
		_placementId = placementId;
	}

	public String getPlacementId() {
		return _placementId;
	}

	public QueryInfo getQueryInfo() {
		return _queryInfo;
	}

	public String getQueryStr() {
		String query = null;
		if (_queryInfo != null) {
			query = _queryInfo.getQuery();
		}
		return query;
	}

	public String getError() {
		return _error;
	}

	public void setQueryInfo(QueryInfo queryInfo) {
		_queryInfo = queryInfo;
	}

	public void setError(String error) {
		_error = error;
	}
}
