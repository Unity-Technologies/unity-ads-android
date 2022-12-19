package com.unity3d.services.ads.gmascar.adapters;

import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.SdkProperties;

public class ScarAdapterFactory {

	public IScarAdapter createScarAdapter(ScarAdapterVersion adapterVersion, IAdsErrorHandler adsErrorHandler) {
		IScarAdapter scarAdapter = null;

		switch (adapterVersion) {
			case V192:
				scarAdapter = new com.unity3d.scar.adapter.v1920.ScarAdapter(adsErrorHandler);
				break;
			case V195:
				scarAdapter = new com.unity3d.scar.adapter.v1950.ScarAdapter(adsErrorHandler);
				break;
			case V20:
				scarAdapter = new com.unity3d.scar.adapter.v2000.ScarAdapter(adsErrorHandler);
				break;
			case V21:
				scarAdapter = new com.unity3d.scar.adapter.v2100.ScarAdapter(adsErrorHandler, SdkProperties.getVersionName());
				break;
			case NA: default:
				reportAdapterFailure(adapterVersion, adsErrorHandler);
		}

		return scarAdapter;
	}

	private void reportAdapterFailure(ScarAdapterVersion adapterVersion, IAdsErrorHandler adsErrorHandler) {
		String errorMessage = String.format("SCAR version %s is not supported.", adapterVersion.name());
		adsErrorHandler.handleError(GMAAdsError.AdapterCreationError(errorMessage));
		DeviceLog.debug(errorMessage);
	}
}
