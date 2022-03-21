package com.unity3d.services.core.device.reader.pii;

import com.unity3d.services.core.device.Device;

public class PiiDataProvider {

	public String getAdvertisingTrackingId() {
		return Device.getAdvertisingTrackingId();
	}
}
