package com.unity3d.ads.test.instrumentation.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.managers.BiddingLazyManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BiddingLazyManagerTest {

	@Mock
	IUnityAdsTokenListener callerListener;
	private BiddingLazyManager manager;
	private String TEST_TOKEN = "token";

	@Before
	public void setup() {
		manager = Mockito.spy(new BiddingLazyManager(callerListener));
	}

	@Test
	public void testNoActionAfterStartCalled() {
		manager.start();
		Mockito.verify(manager, Mockito.times(0)).fetchSignals();
		Mockito.verify(manager, Mockito.times(0)).permitSignalsUpload();
	}

	@Test
	public void testPermitAndFetchSignalsAfterStartCalled() {
		manager.onUnityTokenSuccessfullyFetched();
		Mockito.verify(manager, Mockito.times(1)).fetchSignals();
		Mockito.verify(manager, Mockito.times(1)).permitSignalsUpload();
	}

	@Test
	public void testListenerIsInvokedOnUnityAdsTokenReady() {
		manager.onUnityAdsTokenReady(TEST_TOKEN);
		Mockito.verify(callerListener, Mockito.times(1)).onUnityAdsTokenReady(TEST_TOKEN);
	}

}
