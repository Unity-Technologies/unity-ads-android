package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.Map;

public class DeviceInfoReaderWithMetrics implements IDeviceInfoReader{

	private final IDeviceInfoReader _deviceInfoReader;

	public DeviceInfoReaderWithMetrics(IDeviceInfoReader deviceInfoReader) {
		_deviceInfoReader = deviceInfoReader;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		if (_deviceInfoReader == null) return null;
		Map<String, Object> deviceInfo = _deviceInfoReader.getDeviceInfoData();
		sendMetrics(deviceInfo);
		return deviceInfo;
	}

	private void sendMetrics(Map<String, Object> deviceInfoData) {
		if (deviceInfoData != null ) {
			Object gameSessionId = deviceInfoData.get("unifiedconfig.data.gameSessionId");
			if (gameSessionId instanceof Long && ((Long) gameSessionId) == 0L) {
				SDKMetrics.getInstance().sendMetric(TSIMetric.newMissingGameSessionId());
			}
		}
	}

}
