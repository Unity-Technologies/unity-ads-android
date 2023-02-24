package com.unity3d.ads.test.instrumentation.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.gmascar.handlers.ScarRewardedAdHandler;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
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
public class ScarRewardedAdHandlerTest {

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
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdLoaded();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.GMA), eq(GMAEvent.AD_LOADED), eq("test-placementId"), eq("test-queryId"));
	}

	@Test
	public void testOnAdFailedToLoad() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdFailedToLoad(0, "Ad failed to load");

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.GMA), eq(GMAEvent.LOAD_ERROR), eq("test-placementId"), eq("test-queryId"), eq("Ad failed to load"), eq(0));
	}

	@Test
	public void testOnAdOpened() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdOpened();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_STARTED);
		Mockito.verify(mockEventSubject, times(1)).subscribe(Mockito.<IEventListener>any());
	}

	@Test
	public void testOnAdFailedToShow() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdFailedToShow(0, "Ad failed to show");

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(eq(WebViewEventCategory.GMA), eq(GMAEvent.REWARDED_SHOW_ERROR), eq("test-placementId"), eq("test-queryId"), eq("Ad failed to show"), eq(0));
	}

	@Test
	public void testOnAdSkipped() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdClosed();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_SKIPPED);
	}

	@Test
	public void testOnAdClosedAndRewardGranted() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onUserEarnedReward();
		adHandler.onAdClosed();

		Mockito.verify(mockWebViewApp, times(0)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_SKIPPED);
		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_CLOSED);
		Mockito.verify(mockEventSubject, times(1)).unsubscribe();
	}

	@Test
	public void testOnAdImpression() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdImpression();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.REWARDED_IMPRESSION_RECORDED);
	}

	@Test
	public void testOnAdClicked() {
		ScarRewardedAdHandler adHandler = new ScarRewardedAdHandler(getDefaultScarMeta(), mockEventSubject, new GMAEventSender());
		adHandler.onAdClicked();

		Mockito.verify(mockWebViewApp, times(1)).sendEvent(WebViewEventCategory.GMA, GMAEvent.AD_CLICKED);
	}

	private ScarAdMetadata getDefaultScarMeta() {
		return new ScarAdMetadata("test-placementId", "test-queryId", "test-adUnitId", "test-scarAdString", 30000);
	}
}
