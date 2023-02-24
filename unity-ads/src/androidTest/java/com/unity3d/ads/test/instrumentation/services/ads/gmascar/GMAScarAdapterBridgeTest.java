package com.unity3d.ads.test.instrumentation.services.ads.gmascar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import android.app.Activity;
import android.content.Context;

import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.services.ads.gmascar.GMAScarAdapterBridge;
import com.unity3d.services.ads.gmascar.adapters.ScarAdapterFactory;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridgeBase;
import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;
import com.unity3d.services.ads.gmascar.handlers.BiddingSignalsHandler;
import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GMAScarAdapterBridgeTest {

	@Mock
	BiddingSignalsHandler handler;

	@Mock
	MobileAdsBridgeBase mobileAdsBridgeBase;

	@Mock
	ScarAdapterFactory scarAdapterFactory;

	@Mock
	InitializeListenerBridge initializeListenerBridgeMock;

	@Mock
	InitializationStatusBridge initializationStatusBridgeMock;

	@Mock
	AdapterStatusBridge adapterStatusBridgeMock;

	@Mock
	WebViewErrorHandler webViewErrorHandlerMock;

	@Mock
	GMAEventSender gmaEventSenderMock;

	private static final String SIGNAL = "signal";
	private static final ScarAdapterVersion VERSION = ScarAdapterVersion.V21;
	private GMAScarAdapterBridge gmaScarAdapterBridge;
	private IScarAdapter scarAdapter;

	@Before
	public void setup() {
		scarAdapter = new IScarAdapter() {
			@Override
			public void getSCARSignals(Context context, String[] strings,
									   String[] strings1,
									   ISignalCollectionListener iSignalCollectionListener) {

			}

			@Override
			public void getSCARBiddingSignals(Context context,
											  ISignalCollectionListener iSignalCollectionListener) {
				iSignalCollectionListener.onSignalsCollected(SIGNAL);
			}

			@Override
			public void loadInterstitialAd(Context context,
										   ScarAdMetadata scarAdMetadata,
										   IScarInterstitialAdListenerWrapper iScarInterstitialAdListenerWrapper) {

			}

			@Override
			public void loadRewardedAd(Context context,
									   ScarAdMetadata scarAdMetadata,
									   IScarRewardedAdListenerWrapper iScarRewardedAdListenerWrapper) {

			}

			@Override
			public void show(Activity activity, String s, String s1) {

			}
		};
	}

	@Test
	public void testHasScarBiddingSupportWhenMobileAdsBridgeIsNull() {
		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			null,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		Assert.assertEquals(gmaScarAdapterBridge.hasSCARBiddingSupport(), false);
	}

	@Test
	public void testHasScarBiddingSupportWhenMobileAdsBridgeHasSupport() {
		when(mobileAdsBridgeBase.hasSCARBiddingSupport()).thenReturn(true);
		when(mobileAdsBridgeBase.getAdapterVersion(anyInt())).thenReturn(ScarAdapterVersion.V21);
		when(scarAdapterFactory.createScarAdapter(any(ScarAdapterVersion.class),
			any(WebViewErrorHandler.class))).thenReturn(scarAdapter);

		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			mobileAdsBridgeBase,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		Assert.assertEquals(gmaScarAdapterBridge.hasSCARBiddingSupport(), true);
	}

	@Test
	public void testHasScarBiddingSupportWhenMobileAdsBridgeHasNoSupport() {
		when(mobileAdsBridgeBase.hasSCARBiddingSupport()).thenReturn(false);

		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			mobileAdsBridgeBase,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		Assert.assertEquals(gmaScarAdapterBridge.hasSCARBiddingSupport(), false);
	}

	@Test
	public void testHasScarBiddingSupportWhenAdapterCannotBeCreated() {
		when(mobileAdsBridgeBase.hasSCARBiddingSupport()).thenReturn(true);
		when(mobileAdsBridgeBase.getAdapterVersion(anyInt())).thenReturn(ScarAdapterVersion.NA);

		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			mobileAdsBridgeBase,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		Assert.assertEquals(gmaScarAdapterBridge.hasSCARBiddingSupport(), false);
	}

	@Test
	public void testSignalsCollectedWhenGetScarBiddingSignalsCalled() {
		when(mobileAdsBridgeBase.hasSCARBiddingSupport()).thenReturn(true);
		when(mobileAdsBridgeBase.getAdapterVersion(anyInt())).thenReturn(VERSION);
		when(scarAdapterFactory.createScarAdapter(any(ScarAdapterVersion.class),
			any(WebViewErrorHandler.class))).thenReturn(scarAdapter);

		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			mobileAdsBridgeBase,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		gmaScarAdapterBridge.getSCARBiddingSignals(handler);
		verify(handler, times(0)).onSignalsCollectionFailed(anyString());
		verify(handler, times(1)).onSignalsCollected(SIGNAL);
	}

	@Test
	public void testFailToCollectSignalsWhenNoBiddingSupport() {
		when(mobileAdsBridgeBase.hasSCARBiddingSupport()).thenReturn(false);

		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			mobileAdsBridgeBase,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		gmaScarAdapterBridge.getSCARBiddingSignals(handler);
		verify(handler, times(0)).onSignalsCollected(anyString());
		verify(handler, times(1)).onSignalsCollectionFailed("SCAR bidding unsupported.");
	}

	@Test
	public void testFailToCollectSignalsWhenAdapterCannotBeCreated() {
		when(mobileAdsBridgeBase.hasSCARBiddingSupport()).thenReturn(true);
		when(mobileAdsBridgeBase.getAdapterVersion(anyInt())).thenReturn(ScarAdapterVersion.NA);

		gmaScarAdapterBridge = new GMAScarAdapterBridge(
			mobileAdsBridgeBase,
			initializeListenerBridgeMock,
			initializationStatusBridgeMock,
			adapterStatusBridgeMock,
			webViewErrorHandlerMock,
			scarAdapterFactory,
			gmaEventSenderMock
		);

		gmaScarAdapterBridge.getSCARBiddingSignals(handler);
		verify(handler, times(0)).onSignalsCollected(SIGNAL);
		verify(handler, times(1)).onSignalsCollectionFailed("Could not create SCAR adapter object.");
	}
}
