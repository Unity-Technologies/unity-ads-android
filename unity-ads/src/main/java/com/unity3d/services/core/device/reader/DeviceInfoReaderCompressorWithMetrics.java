package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.Map;

public class DeviceInfoReaderCompressorWithMetrics implements IDeviceInfoDataCompressor {

	private final IDeviceInfoDataCompressor _deviceInfoDataCompressor;
	private final Experiments _experiments;

	private long _startTimeInfo;
	private long _startTimeCompression;
	private long _endTime;

	public DeviceInfoReaderCompressorWithMetrics(IDeviceInfoDataCompressor deviceInfoDataCompressor, Experiments experiments) {
		_deviceInfoDataCompressor = deviceInfoDataCompressor;
		_experiments = experiments;
	}

	@Override
	public byte[] getDeviceData() {
		if (_deviceInfoDataCompressor == null) return new byte[0];
		_startTimeInfo = System.currentTimeMillis();
		Map<String, Object> deviceInfo = getDeviceInfo();
		byte[] zippedData = compressDeviceInfo(deviceInfo);
		sendDeviceInfoMetrics();
		return zippedData;
	}

	@Override
	public Map<String, Object> getDeviceInfo() {
		return _deviceInfoDataCompressor.getDeviceInfo();
	}

	@Override
	public byte[] compressDeviceInfo(Map<String, Object> deviceData) {
		_startTimeCompression = System.currentTimeMillis();
		byte[] zippedData = _deviceInfoDataCompressor.compressDeviceInfo(getDeviceInfo());
		_endTime = System.currentTimeMillis();
		return zippedData;
	}

	private long getDeviceInfoCollectionDuration() {
		return _startTimeCompression - _startTimeInfo;
	}

	private long getCompressionDuration() {
		return _endTime - _startTimeCompression;
	}

	private void sendDeviceInfoMetrics() {
		Map<String, String> tags = null;
		if (_experiments != null) {
			tags = _experiments.getExperimentTags();
		}

		SDKMetrics.getInstance().sendMetric(TSIMetric.newDeviceInfoCollectionLatency(getDeviceInfoCollectionDuration(), tags));
		SDKMetrics.getInstance().sendMetric(TSIMetric.newDeviceInfoCompressionLatency(getCompressionDuration(), tags));
	}
}
