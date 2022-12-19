package com.unity3d.ads.test.instrumentation.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.gmascar.handlers.ScarInterstitialAdHandler;
import com.unity3d.services.core.misc.EventSubject;
import com.unity3d.services.core.misc.IEventListener;
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
public class ScarInterstitialAdHandlerTest {

	@Mock
	WebViewApp mockWebViewApp;

	@Mock
	EventSubject mockEventSubject;

	@Before
	public void setup() {
		WebViewApp.setCurrentApp(mockWebViewApp);
	}

	@Test
	public void testOnAdLoaded() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdLoaded();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.GMA), eq(GMAEvent.AD_LOADED), eq("test-placementId"), eq("test-queryId"));
	}

	@Test
	public void testOnAdFailedToLoad() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdFailedToLoad(0, "Ad failed to load");

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.GMA), eq(GMAEvent.LOAD_ERROR), eq("test-placementId"), eq("test-queryId"), eq("Ad failed to load"), eq(0));
	}

	@Test
	public void testOnAdOpened() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdOpened();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_STARTED);
		Mockito.verify(mockEventSubject, times(1)).subscribe(Mockito.<IEventListener>any());
	}

	@Test
	public void testOnAdFailedToShow() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdFailedToShow(0, "Ad failed to show");

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.GMA), eq(GMAEvent.INTERSTITIAL_SHOW_ERROR), eq("test-placementId"), eq("test-queryId"), eq("Ad failed to show"), eq(0));
	}

	@Test
	public void testOnAdSkipped() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		Mockito.when(mockEventSubject.eventQueueIsEmpty()).thenReturn(false);
		adHandler.onAdClosed();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_SKIPPED);
	}

	@Test
	public void testOnAdClosed() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdClosed();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_CLOSED);
		Mockito.verify(mockEventSubject, times(1)).unsubscribe();
	}

	@Test
	public void testOnAdImpression() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdImpression();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.INTERSTITIAL_IMPRESSION_RECORDED);
	}

	@Test
	public void testOnAdClicked() {
		ScarInterstitialAdHandler adHandler = new ScarInterstitialAdHandler(getDefaultScarMeta(), mockEventSubject);
		adHandler.onAdClicked();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_CLICKED);
	}

	private ScarAdMetadata getDefaultScarMeta() {
		return new ScarAdMetadata("test-placementId", "test-queryId", "test-adUnitId", "test-scarAdString", 30000);
	}
}
