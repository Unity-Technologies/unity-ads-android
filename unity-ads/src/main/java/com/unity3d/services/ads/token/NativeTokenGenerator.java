package com.unity3d.services.ads.token;

import android.util.Base64;

import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressor;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.log.DeviceLog;

import java.util.concurrent.ExecutorService;

public class NativeTokenGenerator implements INativeTokenGenerator {
	private static final String DEFAULT_NATIVE_TOKEN_PREFIX = "1:";
	private ExecutorService _executorService;
	private DeviceInfoReaderBuilder _deviceInfoReaderBuilder;
	private String _prependStr;

	public NativeTokenGenerator(ExecutorService executorService, DeviceInfoReaderBuilder deviceInfoReaderBuilder) {
		this(executorService, deviceInfoReaderBuilder, DEFAULT_NATIVE_TOKEN_PREFIX);
	}

	public NativeTokenGenerator(ExecutorService executorService, DeviceInfoReaderBuilder deviceInfoReaderBuilder, String prependStr) {
		_executorService = executorService;
		_deviceInfoReaderBuilder = deviceInfoReaderBuilder;
		_prependStr = prependStr;
	}

	@Override
	public void generateToken(final INativeTokenGeneratorListener callback) {
		_executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					IDeviceInfoReader deviceInfoReader = _deviceInfoReaderBuilder.build();

					String queryData = Base64.encodeToString(new DeviceInfoReaderCompressor(deviceInfoReader).getDeviceData(), Base64.NO_WRAP);
					if (_prependStr != null && !_prependStr.isEmpty()) {
						StringBuilder stringBuilder = new StringBuilder(_prependStr.length() + queryData.length());
						stringBuilder.append(_prependStr);
						stringBuilder.append(queryData);
						callback.onReady(stringBuilder.toString());
					} else {
						callback.onReady(queryData);
					}
				} catch (Exception e) {
					DeviceLog.exception("Unity Ads failed to generate token.", e);
					callback.onReady(null);
				}
			}
		});
	}
}
