package com.unity3d.ads.test.instrumentation.services.core.device;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.unity3d.services.ads.token.INativeTokenGeneratorListener;
import com.unity3d.services.ads.token.NativeTokenGenerator;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

@RunWith(MockitoJUnitRunner.class)
public class NativeTokenGeneratorTest {
	@Mock
	ExecutorService _executorService;
	@Mock
	IDeviceInfoReader _deviceInfoReader;
	@Mock
	INativeTokenGeneratorListener _callback;

	@Test
	public void testGenerateToken() {
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				invocation.getArgument(0, Runnable.class).run();
				return null;
			}}).when(_executorService).execute(any(Runnable.class));

		when(_deviceInfoReader.getDeviceInfoData()).thenReturn(new HashMap<String, Object>());

		NativeTokenGenerator nativeTokenGenerator = new NativeTokenGenerator(_executorService, _deviceInfoReader);
		nativeTokenGenerator.generateToken(_callback);

		Mockito.verify(_callback, times(1))
			.onReady("1:H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA==");
		Mockito.verify(_callback, times(1))
			.onReady(anyString());
	}

	@Test
	public void testGenerateTokenWithException() {
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				invocation.getArgument(0, Runnable.class).run();
				return null;
			}}).when(_executorService).execute(any(Runnable.class));

		when(_deviceInfoReader.getDeviceInfoData()).thenThrow(new RuntimeException());

		NativeTokenGenerator nativeTokenGenerator = new NativeTokenGenerator(_executorService, _deviceInfoReader);
		nativeTokenGenerator.generateToken(_callback);

		Mockito.verify(_callback, times(1))
			.onReady(null);
		Mockito.verify(_callback, times(1))
			.onReady(nullable(String.class));
	}
}
