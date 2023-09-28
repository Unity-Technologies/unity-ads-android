package com.unity3d.ads.test.instrumentation.services.ads.gmascar.handlers;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

import com.unity3d.services.ads.gmascar.handlers.BiddingSignalsHandler;
import com.unity3d.services.ads.gmascar.listeners.IBiddingSignalsListener;
import com.unity3d.services.ads.gmascar.models.BiddingSignals;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BiddingSignalsHandlerTest {

	private BiddingSignalsHandler biddingSignalsHandler;
	private IBiddingSignalsListener gmaScarBiddingSignalsListener;

	@Test
	public void testSignalsAreSetAfterOnSignalsCollectedWithValidMap() {
		String map = "{\"gmaScarBiddingRewardedSignal\": \"rvSig\",\"gmaScarBiddingInterstitialSignal\": \"inSig\", \"gmaScarBiddingBannerSignal\": \"bnSig\"}";
		gmaScarBiddingSignalsListener = spy(new IBiddingSignalsListener() {
			@Override
			public void onSignalsReady(BiddingSignals signals) {
				assertEquals("rvSig", signals.getRvSignal());
				assertEquals("inSig", signals.getInterstitialSignal());
				assertEquals("bnSig", signals.getBannerSignal());
			}

			@Override
			public void onSignalsFailure(String msg) {
				fail();
			}
		});
		biddingSignalsHandler = new BiddingSignalsHandler(true, gmaScarBiddingSignalsListener);

		biddingSignalsHandler.onSignalsCollected(map);
		verify(gmaScarBiddingSignalsListener, times(1)).onSignalsReady(
			isA(BiddingSignals.class));
	}

	@Test
	public void testNoInterstitialSignalSetAfterOnSignalsCollectedWithMissingInterstitial() {
		String map = "{\"gmaScarBiddingRewardedSignal\": \"rvSig\", \"gmaScarBiddingBannerSignal\": \"bnSig\"}";

		gmaScarBiddingSignalsListener = spy(new IBiddingSignalsListener() {
			@Override
			public void onSignalsReady(BiddingSignals signals) {
				assertEquals("rvSig", signals.getRvSignal());
				assertEquals("", signals.getInterstitialSignal());
				assertEquals("bnSig", signals.getBannerSignal());
			}

			@Override
			public void onSignalsFailure(String msg) {
				fail();
			}
		});
		biddingSignalsHandler = new BiddingSignalsHandler(true, gmaScarBiddingSignalsListener);

		biddingSignalsHandler.onSignalsCollected(map);
		verify(gmaScarBiddingSignalsListener, times(1)).onSignalsReady(
			isA(BiddingSignals.class));
	}

	@Test
	public void testNoBannerSignalSetAfterOnSignalsCollectedWithBannerDisabled() {
		String map = "{\"gmaScarBiddingRewardedSignal\": \"rvSig\",\"gmaScarBiddingInterstitialSignal\": \"inSig\"}";

		gmaScarBiddingSignalsListener = spy(new IBiddingSignalsListener() {
			@Override
			public void onSignalsReady(BiddingSignals signals) {
				assertEquals("rvSig", signals.getRvSignal());
				assertEquals("inSig", signals.getInterstitialSignal());
				assertEquals("", signals.getBannerSignal());
			}

			@Override
			public void onSignalsFailure(String msg) {
				fail();
			}
		});
		biddingSignalsHandler = new BiddingSignalsHandler(false, gmaScarBiddingSignalsListener);

		biddingSignalsHandler.onSignalsCollected(map);
		verify(gmaScarBiddingSignalsListener, times(1)).onSignalsReady(
			isA(BiddingSignals.class));
	}

	@Test
	public void testNoBannerSignalSetAfterOnSignalsCollectedWithBannerPresent() {
		String map = "{\"gmaScarBiddingRewardedSignal\": \"rvSig\",\"gmaScarBiddingInterstitialSignal\": \"inSig\", \"gmaScarBiddingBannerSignal\": \"bnSig\"}";

		gmaScarBiddingSignalsListener = spy(new IBiddingSignalsListener() {
			@Override
			public void onSignalsReady(BiddingSignals signals) {
				assertEquals("rvSig", signals.getRvSignal());
				assertEquals("inSig", signals.getInterstitialSignal());
				assertEquals("", signals.getBannerSignal());
			}

			@Override
			public void onSignalsFailure(String msg) {
				fail();
			}
		});
		biddingSignalsHandler = new BiddingSignalsHandler(false, gmaScarBiddingSignalsListener);

		biddingSignalsHandler.onSignalsCollected(map);
		verify(gmaScarBiddingSignalsListener, times(1)).onSignalsReady(
			isA(BiddingSignals.class));
	}

	@Test
	public void testSignalsAreNullAfterOnSignalsCollectedWithEmptyMap() {
		String map = "";

		gmaScarBiddingSignalsListener = spy(new IBiddingSignalsListener() {
			@Override
			public void onSignalsReady(BiddingSignals signals) {
				assertEquals(null, signals);
			}

			@Override
			public void onSignalsFailure(String msg) {
				fail();
			}
		});
		biddingSignalsHandler = new BiddingSignalsHandler(false, gmaScarBiddingSignalsListener);

		biddingSignalsHandler.onSignalsCollected(map);
		verify(gmaScarBiddingSignalsListener, times(1)).onSignalsReady(
			ArgumentMatchers.<BiddingSignals>isNull());
	}
}
