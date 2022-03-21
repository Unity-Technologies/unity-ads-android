package com.unity3d.services.core.request.metrics;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import static java.lang.Thread.currentThread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(MockitoJUnitRunner.class)
public class MetricSenderWithBatchTest{

	@Mock
	public ISDKMetrics _original;
	@InjectMocks
	public MetricSenderWithBatch _batchedSender;

	@Test
	public void testBatchEventsWhenNoMetricUrlIsSet() {

		_batchedSender.sendEvent("test1");
		_batchedSender.sendEvent("test2");

		Mockito.verify(_original, never()).sendMetrics(Mockito.<Metric>anyList());
	}

	@Test
	public void testDoesNotSendEventsWhenQueueIfEmptyAfterUrlSet() {
		Mockito.when(_original.getMetricEndPoint()).thenReturn("url");

		_batchedSender.sendQueueIfNeeded();

		Mockito.verify(_original, never()).sendMetrics(Mockito.<Metric>anyList());
		assertEquals("url", _batchedSender.getMetricEndPoint());
	}

	@Test
	public void testQueueSentWhenMetricEndpointGetsUpdated() {
		_batchedSender.sendEvent("test1");
		_batchedSender.sendEvent("test2");

		Mockito.when(_original.getMetricEndPoint()).thenReturn("url");

		_batchedSender.sendQueueIfNeeded();

		Mockito.verify(_original, Mockito.times(1)).sendMetrics(Mockito.<Metric>anyList());
	}

	@Test
	public void testSendMetricsNoConcurrency() {
		final int METRICS_SIZE = 1000;
		final ArgumentCaptor<List<Metric>> metricsCapture = ArgumentCaptor.forClass((Class) List.class);

		Mockito.when(_original.getMetricEndPoint()).thenReturn("url");

		MetricSenderWithBatch senderWithBatch = new MetricSenderWithBatch(_original);
		List<Metric> metrics = new ArrayList<>();

		for (int i = 0; i < METRICS_SIZE; i++) {
			metrics.add(new Metric(String.valueOf(i), i, null));
		}

		senderWithBatch.sendMetrics(metrics);

		Mockito.verify(_original, Mockito.times(1)).sendMetrics(Mockito.<Metric>anyList());
		Mockito.verify(_original).sendMetrics(metricsCapture.capture());
		assertEquals(METRICS_SIZE, metricsCapture.getValue().size());
	}

	@Test
	public void testSendMetricsConcurrency() throws InterruptedException{
		final int METRICS_SIZE = 1000;
		final int NUM_THREADS = 100;

		final ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
		final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
		final ArgumentCaptor<List<Metric>> metricsCapture = ArgumentCaptor.forClass((Class) List.class);

		Mockito.when(_original.getMetricEndPoint()).thenReturn("url");

		final MetricSenderWithBatch senderWithBatch = new MetricSenderWithBatch(_original);

		for (int i = 0; i < NUM_THREADS; i++) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					List<Metric> metrics = new ArrayList<>();
					for (int i = 0; i < METRICS_SIZE / NUM_THREADS; i++) {
						metrics.add(new Metric(currentThread().getName(), i, null));
					}
					senderWithBatch.sendMetrics(metrics);
					latch.countDown();
				}
			});
		}
		latch.await();

		Mockito.verify(_original, times(NUM_THREADS)).sendMetrics(Mockito.<Metric>anyList());
		Mockito.verify(_original, times(NUM_THREADS)).sendMetrics(metricsCapture.capture());
		List<List<Metric>> metricsSent = metricsCapture.getAllValues();
		int metricsCount = 0;
		for (List<Metric> lm: metricsSent) {
			for (Metric m: lm) {
				metricsCount++;
			}
		}
		assertEquals(METRICS_SIZE, metricsCount);
	}
}