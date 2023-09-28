package com.unity3d.ads.test.instrumentation.services.ads.gmascar.managers;

import static org.mockito.ArgumentMatchers.any;

import android.app.Application;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.managers.BiddingBaseManager;
import com.unity3d.services.ads.gmascar.models.BiddingSignals;
import com.unity3d.services.ads.gmascar.utils.ScarRequestHandler;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.ScarMetric;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BiddingBaseManagerTest {

	@Mock
	IUnityAdsTokenListener publisherListener;

	@Mock
    SDKMetricsSender _metricSenderMock;

	@Mock
	ScarRequestHandler _scarRequestHandlerMock;

	private BiddingBaseManager managerWithTokenListener;
	private BiddingBaseManager managerWithNullListener;
	private boolean isBannerEnabled = true;
	private String TEST_TOKEN = "token";
	private boolean isAsyncTokenCall = true;
	private boolean isNotAsyncTokenCall = false;


	@Before
	public void setup() {
		ClientProperties.setApplication((Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		managerWithTokenListener = Mockito.spy(new BiddingBaseManager(isBannerEnabled, publisherListener) {
			@Override
			public void start() {

			}
		});

		managerWithNullListener = Mockito.spy(new BiddingBaseManager(isBannerEnabled, null) {
			@Override
			public void start() {

			}
		});

		Mockito.when(managerWithTokenListener.getMetricSender()).thenReturn(_metricSenderMock);
		Mockito.when(managerWithNullListener.getMetricSender()).thenReturn(_metricSenderMock);
	}

	@Test
	public void testSendsAsyncMetricWhenFetchStartsWithAsyncTokenCall() {
		managerWithTokenListener.fetchSignals();

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchStart(isAsyncTokenCall);
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
		Assert.assertEquals(desiredMetric.getValue(), capturedMetric.getValue());
	}

	@Test
	public void testSendsAsyncMetricWhenFetchSucceedsWithAsyncTokenCall() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchSuccess(isAsyncTokenCall);

		managerWithTokenListener.sendFetchResult("");
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
	}

	@Test
	public void testSendsAsyncMetricWhenFetchFailsWithAsyncTokenCall() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchFailure(isAsyncTokenCall, "ERROR");

		managerWithTokenListener.sendFetchResult("ERROR");
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
	}

	@Test
	public void testSendsAsyncMetricWhenUploadStartsAndSucceedsWithAsyncTokenCall() throws InterruptedException {
		BiddingBaseManager managerWithScarRequestSender = Mockito.spy(new BiddingBaseManager(isBannerEnabled, publisherListener, _scarRequestHandlerMock) {
			@Override
			public void start() {

			}
		});
		Mockito.when(managerWithScarRequestSender.getMetricSender()).thenReturn(_metricSenderMock);

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart(isAsyncTokenCall);
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadSuccess(isAsyncTokenCall);

		managerWithScarRequestSender.permitUpload();
		managerWithScarRequestSender.onSignalsReady(new BiddingSignals("testRewardedSignal", "testInterstitialSignal", "testBannerSignal"));

		Thread.sleep(1000);

		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testSendsAsyncMetricWhenUploadFailsWithNullSignalsAndAsyncTokenCall() {
		managerWithTokenListener.uploadSignals();

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart(isAsyncTokenCall);
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadFailure(isAsyncTokenCall, "null or empty signals");
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testSendsAsyncMetricWhenUploadRequestFailsWithMalformedUrlAndAsyncTokenCall() throws Exception {
		String errorMessage = "bad request";
		Mockito.doThrow(new Exception(errorMessage)).when(_scarRequestHandlerMock).makeUploadRequest(Mockito.<String>any(), Mockito.<BiddingSignals>any(), any(String.class));
		BiddingBaseManager managerWithScarRequestSender = Mockito.spy(new BiddingBaseManager(isBannerEnabled, publisherListener, _scarRequestHandlerMock) {
			@Override
			public void start() {

			}
		});
		Mockito.when(managerWithScarRequestSender.getMetricSender()).thenReturn(_metricSenderMock);

		managerWithScarRequestSender.permitUpload();
		managerWithScarRequestSender.onSignalsReady(new BiddingSignals("testRewardedSignal", "testInterstitialSignal", "testBannerSignal"));

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart(isAsyncTokenCall);
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadFailure(isAsyncTokenCall, errorMessage);

		Thread.sleep(1000);

		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testSendsSyncMetricWhenFetchStartsWithSyncTokenCall() {
		managerWithNullListener.fetchSignals();

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchStart(isNotAsyncTokenCall);
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
		Assert.assertEquals(desiredMetric.getValue(), capturedMetric.getValue());
	}

	@Test
	public void testSendsSyncMetricWhenFetchSucceedsWithSyncTokenCall() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchSuccess(isNotAsyncTokenCall);

		managerWithNullListener.sendFetchResult("");
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
	}

	@Test
	public void testSendsSyncMetricWhenFetchFailsWithSyncTokenCall() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchFailure(isNotAsyncTokenCall, "ERROR");

		managerWithNullListener.sendFetchResult("ERROR");
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
	}

	@Test
	public void testSendsSyncMetricWhenUploadStartsAndSucceedsWithSyncTokenCall() throws InterruptedException {
		BiddingBaseManager managerWithScarRequestSender = Mockito.spy(new BiddingBaseManager(isBannerEnabled, null, _scarRequestHandlerMock) {
			@Override
			public void start() {

			}
		});
		Mockito.when(managerWithScarRequestSender.getMetricSender()).thenReturn(_metricSenderMock);

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart(isNotAsyncTokenCall);
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadSuccess(isNotAsyncTokenCall);

		managerWithScarRequestSender.permitUpload();
		managerWithScarRequestSender.onSignalsReady(new BiddingSignals("testRewardedSignal", "testInterstitialSignal", "testBannerSignal"));

		Thread.sleep(1000);

		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testSendsSyncMetricWhenUploadFailsWithNullSignalsAndSyncTokenCall() {
		managerWithNullListener.uploadSignals();

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart(isNotAsyncTokenCall);
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadFailure(isNotAsyncTokenCall, "null or empty signals");
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testSendsSyncMetricWhenUploadRequestFailsWithMalformedUrlAndSyncTokenCall() throws Exception {
		String errorMessage = "bad request";
		Mockito.doThrow(new Exception(errorMessage)).when(_scarRequestHandlerMock).makeUploadRequest(Mockito.<String>any(), Mockito.<BiddingSignals>any(), any(String.class));
		BiddingBaseManager managerWithScarRequestSender = Mockito.spy(new BiddingBaseManager(isBannerEnabled, null, _scarRequestHandlerMock) {
			@Override
			public void start() {

			}
		});
		Mockito.when(managerWithScarRequestSender.getMetricSender()).thenReturn(_metricSenderMock);

		managerWithScarRequestSender.permitUpload();
		managerWithScarRequestSender.onSignalsReady(new BiddingSignals("testRewardedSignal", "testInterstitialSignal", "testBannerSignal"));

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart(isNotAsyncTokenCall);
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadFailure(isNotAsyncTokenCall, errorMessage);

		Thread.sleep(1000);

		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testOnUnityAdsTokenReadyWithListener() {
		managerWithTokenListener.onUnityAdsTokenReady(TEST_TOKEN);
		Mockito.verify(publisherListener, Mockito.times(1)).onUnityAdsTokenReady(TEST_TOKEN);
	}

	@Test
	public void testUploadSignalsAfterPermittedAndSignalsReady() throws InterruptedException {
		managerWithTokenListener.permitSignalsUpload();
		Thread.sleep(100);
		managerWithTokenListener.onSignalsReady(new BiddingSignals("test", "test", "test"));
		Mockito.verify(managerWithTokenListener, Mockito.times(1)).uploadSignals();
	}

	@Test
	public void testNoUploadSignalsAfterPermittedButSignalsNotReady() throws InterruptedException {
		managerWithTokenListener.permitSignalsUpload();
		Thread.sleep(100);
		Mockito.verify(managerWithTokenListener, Mockito.times(0)).uploadSignals();
	}

	@Test
	public void testUploadSignalsAfterSignalsReadyButNotPermitted() throws InterruptedException {
		managerWithTokenListener.onSignalsReady(new BiddingSignals("test", "test", "test"));
		Thread.sleep(100);
		Mockito.verify(managerWithTokenListener, Mockito.times(0)).uploadSignals();
	}

	@Test
	public void testOnlyOneUploadAllowedAfterSignalsReady() throws InterruptedException {
		managerWithTokenListener.permitSignalsUpload();
		Thread.sleep(100);
		Mockito.verify(managerWithTokenListener, Mockito.times(0)).uploadSignals();
		managerWithTokenListener.onSignalsReady(new BiddingSignals("test", "test", "test"));
		managerWithTokenListener.onSignalsReady(new BiddingSignals("test", "test", "test"));
		managerWithTokenListener.onSignalsReady(new BiddingSignals("test", "test", "test"));
		Mockito.verify(managerWithTokenListener, Mockito.times(1)).uploadSignals();
	}
}
