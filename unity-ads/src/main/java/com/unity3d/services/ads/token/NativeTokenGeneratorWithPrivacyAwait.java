package com.unity3d.services.ads.token;

import android.os.ConditionVariable;

import com.unity3d.services.core.misc.IObserver;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;

import java.util.concurrent.ExecutorService;

public class NativeTokenGeneratorWithPrivacyAwait implements INativeTokenGenerator {
	private final INativeTokenGenerator _nativeTokenGenerator;
	private final ConditionVariable _privacyAwait;
	private final ExecutorService _executorService;
	private final int _privacyAwaitTimeout;


	public NativeTokenGeneratorWithPrivacyAwait(ExecutorService executorService, INativeTokenGenerator nativeTokenGenerator, int privacyAwaitTimeout) {
		_executorService = executorService;
		_nativeTokenGenerator = nativeTokenGenerator;
		_privacyAwait = new ConditionVariable();
		_privacyAwaitTimeout = privacyAwaitTimeout;
	}

	@Override
	public void generateToken(final INativeTokenGeneratorListener callback) {
		final IObserver<PrivacyConfig> privacyConfigObserver = new IObserver<PrivacyConfig>() {
			@Override
			public void updated(PrivacyConfig observable) {
				_privacyAwait.open();
			}
		};

		PrivacyConfigStorage.getInstance().registerObserver(privacyConfigObserver);
		_executorService.execute(new Runnable() {
			@Override
			public void run() {
				_privacyAwait.block(_privacyAwaitTimeout);
				PrivacyConfigStorage.getInstance().unregisterObserver(privacyConfigObserver);
				_nativeTokenGenerator.generateToken(callback);
			}
		});
	}
}
