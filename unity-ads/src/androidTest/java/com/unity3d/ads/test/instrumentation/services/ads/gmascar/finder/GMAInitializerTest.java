package com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;
import com.unity3d.services.ads.gmascar.finder.GMAInitializer;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.core.webview.WebViewApp;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class GMAInitializerTest {
	@Mock
	MobileAdsBridge mobileAdsBridge;
	@Mock
	InitializeListenerBridge initializeListenerBridge;
	@Mock
	InitializationStatusBridge initializationStatusBridge;
	@Mock
	AdapterStatusBridge adapterStatusBridge;

	@Test
	public void testGmaInitializer() {
		GMAInitializer gmaInitializer = new GMAInitializer(mobileAdsBridge, initializeListenerBridge, initializationStatusBridge, adapterStatusBridge);
		gmaInitializer.initializeGMA();
		Mockito.verify(mobileAdsBridge, times(1)).initialize(Mockito.any(Context.class), Mockito.any());
	}

	@Test
	public void testGmaInitializerInitSuccess() {
		MobileAdsBridge realMobileAdsBridge = new MobileAdsBridge();
		OnInitializationCompleteListener initializationCompleteListener = Mockito.mock(OnInitializationCompleteListener.class);
		realMobileAdsBridge.initialize(InstrumentationRegistry.getInstrumentation().getContext(), initializationCompleteListener);
		Object initializationStatus = realMobileAdsBridge.getInitializationStatus();

		InitializationStatusBridge realStatusBridge = new InitializationStatusBridge();
		GMAInitializer gmaInitializer = new GMAInitializer(realMobileAdsBridge, initializeListenerBridge, realStatusBridge, adapterStatusBridge);

		WebViewApp mockWebViewApp = Mockito.mock(WebViewApp.class);
		Mockito.when(mockWebViewApp.sendEvent(Mockito.any(Enum.class), Mockito.any(Enum.class))).thenReturn(true);
		WebViewApp.setCurrentApp(mockWebViewApp);
		Mockito.when(adapterStatusBridge.isGMAInitialized(Mockito.any())).thenReturn(true);
		boolean initSuccess = gmaInitializer.initSuccessful(initializationStatus);
		Assert.assertTrue(initSuccess);
	}
}
