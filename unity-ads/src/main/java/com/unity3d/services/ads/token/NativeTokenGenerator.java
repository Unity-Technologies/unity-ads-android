package com.unity3d.services.ads.token;

import android.util.Base64;

import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressor;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.log.DeviceLog;

import java.util.concurrent.ExecutorService;

public class NativeTokenGenerator implements INativeTokenGenerator {
	private ExecutorService _executorService;
	private IDeviceInfoReader _deviceInfoReader;

	public NativeTokenGenerator(ExecutorService executorService, IDeviceInfoReader deviceInfoReader) {
		_executorService = executorService;
		_deviceInfoReader = deviceInfoReader;
	}

	public void generateToken(final INativeTokenGeneratorListener callback) {
		_executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					String queryData = Base64.encodeToString(new DeviceInfoReaderCompressor(_deviceInfoReader).getDeviceData(), Base64.NO_WRAP);

					StringBuilder stringBuilder = new StringBuilder(2 + queryData.length());
					stringBuilder.append("1:");
					stringBuilder.append(queryData);

					callback.onReady(stringBuilder.toString());
				} catch (Exception e) {
					DeviceLog.exception("Unity Ads failed to generate token.", e);
					callback.onReady(null);
				}
			}
		});
	}
}
