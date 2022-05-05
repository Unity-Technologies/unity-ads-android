package com.unity3d.scar.adapter.common;

import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;

public class GMAAdsError extends WebViewAdsError {
	public static final String AD_NOT_LOADED_MESSAGE = "Cannot show ad that is not loaded for placement %s";
	public static final String MISSING_QUERYINFO_MESSAGE = "Missing queryInfoMetadata for ad %s";

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

	public static GMAAdsError AdapterCreationError(String message) {
		return new GMAAdsError(GMAEvent.SCAR_UNSUPPORTED, message);
	}

	public static GMAAdsError NoAdsError(String placementId, String queryId, String message) {
		return new GMAAdsError(GMAEvent.NO_AD_ERROR, message, placementId, queryId, message);
	}

	public static GMAAdsError AdNotLoadedError(ScarAdMetadata scarAdMetadata) {
		String message = String.format(AD_NOT_LOADED_MESSAGE, scarAdMetadata.getPlacementId());
		return new GMAAdsError(GMAEvent.AD_NOT_LOADED_ERROR, message, scarAdMetadata.getPlacementId(), scarAdMetadata.getQueryId(), message);
	}

	public static GMAAdsError InternalShowError(ScarAdMetadata scarAdMetadata, String message) {
		return new GMAAdsError(GMAEvent.INTERNAL_SHOW_ERROR, message, scarAdMetadata.getPlacementId(), scarAdMetadata.getQueryId(), message);
	}

	public static GMAAdsError QueryNotFoundError(ScarAdMetadata scarAdMetadata) {
		String message = String.format(MISSING_QUERYINFO_MESSAGE, scarAdMetadata.getPlacementId());
		return new GMAAdsError(GMAEvent.QUERY_NOT_FOUND_ERROR, message, scarAdMetadata.getPlacementId(), scarAdMetadata.getQueryId(), message);
	}

	public static GMAAdsError InternalLoadError(ScarAdMetadata scarAdMetadata, String message) {
		return new GMAAdsError(GMAEvent.INTERNAL_LOAD_ERROR, message, scarAdMetadata.getPlacementId(), scarAdMetadata.getQueryId(), message);
	}

	public static GMAAdsError InternalSignalsError(String message) {
		return new GMAAdsError(GMAEvent.INTERNAL_SIGNALS_ERROR, message, message);
	}
}
