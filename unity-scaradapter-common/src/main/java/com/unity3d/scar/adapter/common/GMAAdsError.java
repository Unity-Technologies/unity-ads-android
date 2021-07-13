package com.unity3d.scar.adapter.common;

import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;

public class GMAAdsError extends WebViewAdsError {
	public static final String INTERNAL_SHOW_MESSAGE_NOT_LOADED = "Cannot show ad that is not loaded for placement %s";
	public static final String INTERNAL_LOAD_MESSAGE_MISSING_QUERYINFO = "Missing queryInfoMetadata for ad %s";

	public GMAAdsError(GMAEvent errorCategory, Object... errorArguments) {
		super(errorCategory, null, errorArguments);
	}

	public GMAAdsError(GMAEvent errorCategory, String description, Object... errorArguments) {
		super(errorCategory, description, errorArguments);
	}

	@Override
	public String getDomain() {
		return "GMA";
	}


	public static GMAAdsError NoAdsError(String placementId, String queryId, String message) {
		return new GMAAdsError(GMAEvent.NO_AD_ERROR, message, placementId, queryId, message);
	}

	public static GMAAdsError InternalShowError(ScarAdMetadata scarAdMetadata) {
		return InternalShowError(scarAdMetadata, String.format(INTERNAL_SHOW_MESSAGE_NOT_LOADED, scarAdMetadata.getPlacementId()));
	}

	public static GMAAdsError InternalShowError(ScarAdMetadata scarAdMetadata, String message) {
		return new GMAAdsError(GMAEvent.INTERNAL_SHOW_ERROR, message, scarAdMetadata.getPlacementId(), scarAdMetadata.getQueryId(), message);
	}

	public static GMAAdsError InternalLoadError(ScarAdMetadata scarAdMetadata) {
		return InternalLoadError(scarAdMetadata, String.format(INTERNAL_LOAD_MESSAGE_MISSING_QUERYINFO, scarAdMetadata.getPlacementId()));
	}

	public static GMAAdsError InternalLoadError(ScarAdMetadata scarAdMetadata, String message) {
		return new GMAAdsError(GMAEvent.INTERNAL_LOAD_ERROR, message, scarAdMetadata.getPlacementId(), scarAdMetadata.getQueryId(), message);
	}

	public static GMAAdsError InternalSignalsError(String message) {
		return new GMAAdsError(GMAEvent.INTERNAL_SIGNALS_ERROR, message, message);
	}
}
