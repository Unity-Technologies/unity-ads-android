package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DeviceInfoReaderCompressorWithMetrics implements IDeviceInfoDataCompressor {

	private final IDeviceInfoDataCompressor _deviceInfoDataCompressor;

	private long _startTimeInfo;
	private long _startTimeCompression;
	private long _endTime;

	public DeviceInfoReaderCompressorWithMetrics(IDeviceInfoDataCompressor deviceInfoDataCompressor) {
		_deviceInfoDataCompressor = deviceInfoDataCompressor;
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
		SDKMetrics.getInstance().sendMetric(TSIMetric.newDeviceInfoCollectionLatency(getDeviceInfoCollectionDuration()));
		SDKMetrics.getInstance().sendMetric(TSIMetric.newDeviceInfoCompressionLatency(getCompressionDuration()));
	}
}
