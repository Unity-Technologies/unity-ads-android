package com.unity3d.services.ads.gmascar.adapters;

import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;

public class ScarAdapterFactory {
	public static final int CODE_20_0 = 210402000;
	public static final int CODE_19_8 = 204890000;
	public static final int CODE_19_5 = 203404000;
	public static final int CODE_19_2 = 201604000;

	public IScarAdapter createScarAdapter(long gmaVersionCode, IAdsErrorHandler adsErrorHandler) {
		IScarAdapter scarAdapter = null;
		if (gmaVersionCode >= CODE_20_0) {
			scarAdapter = new com.unity3d.scar.adapter.v2000.ScarAdapter(adsErrorHandler);
		} else if (gmaVersionCode >= CODE_19_5 && gmaVersionCode <= CODE_19_8) {
			scarAdapter = new com.unity3d.scar.adapter.v1950.ScarAdapter(adsErrorHandler);
		} else if (gmaVersionCode >= CODE_19_2) {
			scarAdapter = new com.unity3d.scar.adapter.v1920.ScarAdapter(adsErrorHandler);
		} else {
			String errorMessage = String.format("SCAR version %s is not supported.", gmaVersionCode);
			adsErrorHandler.handleError(GMAAdsError.AdapterCreationError(errorMessage));
			DeviceLog.debug(errorMessage);
		}
		return scarAdapter;
	}
}
