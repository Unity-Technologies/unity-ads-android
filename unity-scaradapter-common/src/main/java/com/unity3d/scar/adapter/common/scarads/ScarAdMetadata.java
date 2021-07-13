package com.unity3d.scar.adapter.common.scarads;

public class ScarAdMetadata {

	private String _placementId;
	private String _queryId;
	private String _adUnitId;
	private String _adString;
	private Integer _videoLengthMs;

	public ScarAdMetadata(String placementId, String queryId) {
		this(placementId, queryId, null, null, null);
	}

	public ScarAdMetadata(String placementId, String queryId, String adUnitId, String adString, Integer videoLengthMs) {
		_placementId = placementId;
		_queryId = queryId;
		_adUnitId = adUnitId;
		_adString = adString;
		_videoLengthMs = videoLengthMs;
	}

	public String getPlacementId() {
		return _placementId;
	}

	public String getQueryId() {
		return _queryId;
	}

	public String getAdUnitId() {
		return _adUnitId;
	}

	public String getAdString() {
		return _adString;
	}

	public Integer getVideoLengthMs() {
		return _videoLengthMs;
	}

}
