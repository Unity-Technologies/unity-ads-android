package com.unity3d.ads.test.instrumentation.services.core.device;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.unity3d.services.ads.token.INativeTokenGenerator;
import com.unity3d.services.ads.token.INativeTokenGeneratorListener;
import com.unity3d.services.ads.token.NativeTokenGeneratorWithPrivacyAwait;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(MockitoJUnitRunner.class)
public class NativeTokenGeneratorWithPrivacyAwaitTest {
	private static final int DEFAULT_PRIVACY_TIMEOUT = 3000;
	private static final String MOCK_TOKEN = "1:ABCDEF";

	@Mock
	ExecutorService _executorService;

	@Mock
	INativeTokenGenerator _nativeTokenGeneratorMock;

	@Mock
	INativeTokenGeneratorListener _nativeTokenGeneratorListenerMock;

	@Before
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		PrivacyConfigStorage.getInstance().setPrivacyConfig(new PrivacyConfig());
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				_nativeTokenGeneratorListenerMock.onReady(MOCK_TOKEN);
				return null;
			}
		}).when(_nativeTokenGeneratorMock).generateToken(_nativeTokenGeneratorListenerMock);

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				invocation.getArgument(0, Runnable.class).run();
				return null;
			}}).when(_executorService).execute(any(Runnable.class));
	}

	@Test
	public void testNativeTokenGeneratorWithPrivacyAwait() {
		NativeTokenGeneratorWithPrivacyAwait nativeTokenGeneratorWithPrivacyAwait = new NativeTokenGeneratorWithPrivacyAwait(_executorService, _nativeTokenGeneratorMock, DEFAULT_PRIVACY_TIMEOUT);
		nativeTokenGeneratorWithPrivacyAwait.generateToken(_nativeTokenGeneratorListenerMock);
		PrivacyConfigStorage.getInstance().setPrivacyConfig(new PrivacyConfig());
		Mockito.verify(_nativeTokenGeneratorListenerMock, Mockito.timeout(DEFAULT_PRIVACY_TIMEOUT).times(1)).onReady(MOCK_TOKEN);
		Mockito.verify(_executorService, Mockito.times(1)).execute(Mockito.any(Runnable.class));
	}

	@Test
	public void testNativeTokenGeneratorWithPrivacyAwaitMultipleTimesSingleThreadExecutor() {
		NativeTokenGeneratorWithPrivacyAwait nativeTokenGeneratorWithPrivacyAwait = new NativeTokenGeneratorWithPrivacyAwait(Executors.newSingleThreadExecutor(), _nativeTokenGeneratorMock, DEFAULT_PRIVACY_TIMEOUT);
		nativeTokenGeneratorWithPrivacyAwait.generateToken(_nativeTokenGeneratorListenerMock);
		nativeTokenGeneratorWithPrivacyAwait.generateToken(_nativeTokenGeneratorListenerMock);
		nativeTokenGeneratorWithPrivacyAwait.generateToken(_nativeTokenGeneratorListenerMock);
		PrivacyConfigStorage.getInstance().setPrivacyConfig(new PrivacyConfig());
		Mockito.verify(_nativeTokenGeneratorListenerMock, Mockito.timeout(DEFAULT_PRIVACY_TIMEOUT).times(3)).onReady(MOCK_TOKEN);
	}

	@Test
	public void testNativeTokenGeneratorWithPrivacyAwaitTimeout() {
		NativeTokenGeneratorWithPrivacyAwait nativeTokenGeneratorWithPrivacyAwait = new NativeTokenGeneratorWithPrivacyAwait(_executorService, _nativeTokenGeneratorMock, DEFAULT_PRIVACY_TIMEOUT / 2);
		nativeTokenGeneratorWithPrivacyAwait.generateToken(_nativeTokenGeneratorListenerMock);
		Mockito.verify(_executorService, Mockito.times(1)).execute(Mockito.any(Runnable.class));
		Mockito.verify(_nativeTokenGeneratorListenerMock, Mockito.timeout(DEFAULT_PRIVACY_TIMEOUT).times(1)).onReady(MOCK_TOKEN);
	}

	@Test
	public void testNativeTokenGeneratorWithPrivacyAwaitTimeoutZero() {
		NativeTokenGeneratorWithPrivacyAwait nativeTokenGeneratorWithPrivacyAwait = new NativeTokenGeneratorWithPrivacyAwait(_executorService, _nativeTokenGeneratorMock, 1);
		nativeTokenGeneratorWithPrivacyAwait.generateToken(_nativeTokenGeneratorListenerMock);
		Mockito.verify(_nativeTokenGeneratorListenerMock, Mockito.timeout(DEFAULT_PRIVACY_TIMEOUT).times(1)).onReady(MOCK_TOKEN);
		Mockito.verify(_executorService, Mockito.times(1)).execute(Mockito.any(Runnable.class));
	}
}
