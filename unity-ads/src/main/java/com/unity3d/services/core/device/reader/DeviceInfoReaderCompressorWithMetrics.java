package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
		_startTimeInfo = System.nanoTime();
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
		_startTimeCompression = System.nanoTime();
		byte[] zippedData = _deviceInfoDataCompressor.compressDeviceInfo(deviceData);
		_endTime = System.nanoTime();
		return zippedData;
	}

	private long getDeviceInfoCollectionDuration() {
		return TimeUnit.NANOSECONDS.toMillis(_startTimeCompression - _startTimeInfo);
	}

	private long getCompressionDuration() {
		return TimeUnit.NANOSECONDS.toMillis(_endTime - _startTimeCompression);
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
