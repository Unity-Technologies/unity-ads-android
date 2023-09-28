package com.unity3d.ads.test.instrumentation.services.ads.gmascar.handlers;

import com.unity3d.services.ads.gmascar.handlers.ScarBannerAdHandler;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class ScarBannerAdHandlerTest {

	@Mock
	WebViewApp mockWebViewApp;

	private String testBannerId = "test-bannerId";

	@Before
	public void setup() {
		WebViewApp.setCurrentApp(mockWebViewApp);
	}

	@Test
	public void testOnAdLoaded() {
		ScarBannerAdHandler adHandler = new ScarBannerAdHandler(testBannerId);
		adHandler.onAdLoaded();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.BANNER), eq(BannerBridge.BannerEvent.SCAR_BANNER_LOADED), eq(testBannerId));
	}

	@Test
	public void testOnAdFailedToLoad() {
		int errorCode = 123;
		String errorString = "Test Error Message";

		ScarBannerAdHandler adHandler = new ScarBannerAdHandler(testBannerId);
		adHandler.onAdFailedToLoad(errorCode, errorString);

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(
			eq(WebViewEventCategory.BANNER),
			eq(BannerBridge.BannerEvent.SCAR_BANNER_LOAD_FAILED),
			eq(testBannerId)
		);
	}

	@Test
	public void testOnAdOpened() {
		ScarBannerAdHandler adHandler = new ScarBannerAdHandler(testBannerId);
		adHandler.onAdOpened();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(
			eq(WebViewEventCategory.BANNER),
			eq(BannerBridge.BannerEvent.SCAR_BANNER_OPENED),
			eq(testBannerId)
		);
	}

	@Test
	public void testOnAdClicked() {
		ScarBannerAdHandler adHandler = new ScarBannerAdHandler(testBannerId);
		adHandler.onAdClicked();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(
			eq(WebViewEventCategory.BANNER),
			eq(BannerBridge.BannerEvent.SCAR_BANNER_CLICKED),
			eq(testBannerId)
		);
	}

	@Test
	public void testOnAdClosed() {
		ScarBannerAdHandler adHandler = new ScarBannerAdHandler(testBannerId);
		adHandler.onAdClosed();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(
			eq(WebViewEventCategory.BANNER),
			eq(BannerBridge.BannerEvent.SCAR_BANNER_CLOSED),
			eq(testBannerId)
		);
	}

	@Test
	public void testOnAdImpression() {
		ScarBannerAdHandler adHandler = new ScarBannerAdHandler(testBannerId);
		adHandler.onAdImpression();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(
			eq(WebViewEventCategory.BANNER),
			eq(BannerBridge.BannerEvent.SCAR_BANNER_IMPRESSION),
			eq(testBannerId)
		);
	}
}
