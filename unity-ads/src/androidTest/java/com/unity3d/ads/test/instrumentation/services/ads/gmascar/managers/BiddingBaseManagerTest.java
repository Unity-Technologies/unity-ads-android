package com.unity3d.ads.test.instrumentation.services.ads.gmascar.managers;

import static org.mockito.ArgumentMatchers.any;

import android.app.Application;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.managers.BiddingBaseManager;
import com.unity3d.services.ads.gmascar.models.BiddingSignals;
import com.unity3d.services.ads.gmascar.utils.ScarRequestHandler;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
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
	ISDKMetrics _metricSenderMock;

	@Mock
	ScarRequestHandler _scarRequestHandlerMock;

	private BiddingBaseManager manager;
	private String TEST_TOKEN = "token";

	@Before
	public void setup() {
		ClientProperties.setApplication((Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		manager = Mockito.spy(new BiddingBaseManager(publisherListener) {
			@Override
			public void start() {

			}

			@Override
			public void onUnityTokenSuccessfullyFetched() {

			}
		});

		Mockito.when(manager.getMetricSender()).thenReturn(_metricSenderMock);
	}

	@Test
	public void testSendsMetricWhenFetchStarts() {
		manager.fetchSignals();

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchStart();
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
		Assert.assertEquals(desiredMetric.getValue(), capturedMetric.getValue());
	}

	@Test
	public void testSendsMetricWhenFetchSucceeds() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchSuccess();

		manager.sendFetchResult("");
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
	}

	@Test
	public void testSendsMetricWhenFetchFails() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = ScarMetric.hbSignalsFetchFailure("ERROR");

		manager.sendFetchResult("ERROR");
		Mockito.verify(_metricSenderMock, Mockito.times(1)).sendMetric(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
		Assert.assertEquals(desiredMetric.getTags(), capturedMetric.getTags());
	}

	@Test
	public void testSendsMetricWhenUploadStartsAndSucceeds() throws InterruptedException {
		BiddingBaseManager managerWithScarRequestSender = Mockito.spy(new BiddingBaseManager(publisherListener, _scarRequestHandlerMock) {
			@Override
			public void start() {

			}

			@Override
			public void onUnityTokenSuccessfullyFetched() {

			}
		});
		Mockito.when(managerWithScarRequestSender.getMetricSender()).thenReturn(_metricSenderMock);

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart();
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadSuccess();

		managerWithScarRequestSender.permitUpload();
		managerWithScarRequestSender.onSignalsReady(new BiddingSignals("testRewardedSignal", "testInterstitialSignal"));

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
	public void testSendsMetricWhenUploadFailsWithNullSignals() {
		manager.uploadSignals();

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart();
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadFailure("null or empty signals");
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(metricsCaptor.capture());
		final Metric firstCapturedMetric = metricsCaptor.getAllValues().get(0);
		final Metric secondCapturedMetric = metricsCaptor.getAllValues().get(1);

		Assert.assertEquals(firstDesiredMetric.getName(), firstCapturedMetric.getName());
		Assert.assertEquals(firstDesiredMetric.getTags(), firstCapturedMetric.getTags());

		Assert.assertEquals(secondDesiredMetric.getName(), secondCapturedMetric.getName());
		Assert.assertEquals(secondDesiredMetric.getTags(), secondCapturedMetric.getTags());
	}

	@Test
	public void testSendsMetricWhenUploadRequestFailsWithMalformedUrl() throws Exception {
		String errorMessage = "bad request";
		Mockito.doThrow(new Exception(errorMessage)).when(_scarRequestHandlerMock).makeUploadRequest(Mockito.<String>any(), Mockito.<BiddingSignals>any(), any(String.class));
		BiddingBaseManager managerWithScarRequestSender = Mockito.spy(new BiddingBaseManager(publisherListener, _scarRequestHandlerMock) {
			@Override
			public void start() {

			}

			@Override
			public void onUnityTokenSuccessfullyFetched() {

			}
		});
		Mockito.when(managerWithScarRequestSender.getMetricSender()).thenReturn(_metricSenderMock);

		managerWithScarRequestSender.permitUpload();
		managerWithScarRequestSender.onSignalsReady(new BiddingSignals("testRewardedSignal", "testInterstitialSignal"));

		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric firstDesiredMetric = ScarMetric.hbSignalsUploadStart();
		final Metric secondDesiredMetric = ScarMetric.hbSignalsUploadFailure(errorMessage);

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
		manager.onUnityAdsTokenReady(TEST_TOKEN);
		Mockito.verify(publisherListener, Mockito.times(1)).onUnityAdsTokenReady(TEST_TOKEN);
	}

	@Test
	public void testOnUnityTokenSuccessfullyFetchedAfterOnUnityAdsTokenReadyWithValidToken() {
		manager.onUnityAdsTokenReady(TEST_TOKEN);
		Mockito.verify(manager, Mockito.times(1)).onUnityTokenSuccessfullyFetched();
	}

	@Test
	public void testOnUnityTokenSuccessfullyFetchedAfterOnUnityAdsTokenReadyWithNullToken() {
		manager.onUnityAdsTokenReady(null);
		Mockito.verify(manager, Mockito.times(0)).onUnityTokenSuccessfullyFetched();
	}

	@Test
	public void testOnUnityTokenSuccessfullyFetchedAfterOnUnityAdsTokenReadyWithEmptyToken() {
		manager.onUnityAdsTokenReady("");
		Mockito.verify(manager, Mockito.times(0)).onUnityTokenSuccessfullyFetched();
	}

	@Test
	public void testUploadSignalsAfterPermittedAndSignalsReady() throws InterruptedException {
		manager.permitSignalsUpload();
		Thread.sleep(100);
		manager.onSignalsReady(new BiddingSignals("test", "test"));
		Mockito.verify(manager, Mockito.times(1)).uploadSignals();
	}

	@Test
	public void testNoUploadSignalsAfterPermittedButSignalsNotReady() throws InterruptedException {
		manager.permitSignalsUpload();
		Thread.sleep(100);
		Mockito.verify(manager, Mockito.times(0)).uploadSignals();
	}

	@Test
	public void testUploadSignalsAfterSignalsReadyButNotPermitted() throws InterruptedException {
		manager.onSignalsReady(new BiddingSignals("test", "test"));
		Thread.sleep(100);
		Mockito.verify(manager, Mockito.times(0)).uploadSignals();
	}

	@Test
	public void testOnlyOneUploadAllowedAfterSignalsReady() throws InterruptedException {
		manager.permitSignalsUpload();
		Thread.sleep(100);
		Mockito.verify(manager, Mockito.times(0)).uploadSignals();
		manager.onSignalsReady(new BiddingSignals("test", "test"));
		manager.onSignalsReady(new BiddingSignals("test", "test"));
		manager.onSignalsReady(new BiddingSignals("test", "test"));
		Mockito.verify(manager, Mockito.times(1)).uploadSignals();
	}
}
