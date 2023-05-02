package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.Map;

public class DeviceInfoReaderWithMetrics implements IDeviceInfoReader{

	private final IDeviceInfoReader _deviceInfoReader;
	private final SDKMetricsSender _sdkMetricsSender;

	public DeviceInfoReaderWithMetrics(IDeviceInfoReader deviceInfoReader, SDKMetricsSender sdkMetricsSender) {
		_deviceInfoReader = deviceInfoReader;
		_sdkMetricsSender = sdkMetricsSender;
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
				_sdkMetricsSender.sendMetric(TSIMetric.newMissingGameSessionId());
			}
		}
	}

}
