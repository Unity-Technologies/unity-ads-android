package com.unity3d.services.core.configuration;


import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;

import com.unity3d.services.core.request.metrics.Metric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitializeEventsMetricSenderTest {

	@Mock
	public InitializeEventsMetricSender _metricSenderMock;

	@Test
	public void testSendsMetricWhenInitSuccessfulOnlyOnce() {
		doCallRealMethod().when(_metricSenderMock).didInitStart();
		doCallRealMethod().when(_metricSenderMock).sdkDidInitialize();

		_metricSenderMock.didInitStart();
		_metricSenderMock.sdkDidInitialize();
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));

		_metricSenderMock.sdkDidInitialize();
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));
	}

	@Test
	public void testSendsMetricWhenInitFailsOnlyOnce() {
		doCallRealMethod().when(_metricSenderMock).didInitStart();
		doCallRealMethod().when(_metricSenderMock).sdkInitializeFailed(Mockito.anyString());

		_metricSenderMock.didInitStart();
		_metricSenderMock.sdkInitializeFailed(Mockito.anyString());
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));

		_metricSenderMock.sdkInitializeFailed(Mockito.anyString());
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));
	}

	@Test
	public void testSendsMetricWhenGetsTokenOnlyOnce() {
		doCallRealMethod().when(_metricSenderMock).didInitStart();
		doCallRealMethod().when(_metricSenderMock).didConfigRequestStart();
		doCallRealMethod().when(_metricSenderMock).sdkTokenDidBecomeAvailableWithConfig(true);

		_metricSenderMock.didInitStart();
		_metricSenderMock.didConfigRequestStart();
		_metricSenderMock.sdkTokenDidBecomeAvailableWithConfig(true);

		Mockito.verify(_metricSenderMock, Mockito.times(3)).sendMetric(Mockito.any(Metric.class));

		_metricSenderMock.sdkTokenDidBecomeAvailableWithConfig(false);
		Mockito.verify(_metricSenderMock, Mockito.times(3)).sendMetric(Mockito.any(Metric.class));
	}

	@Test
	public void testSendsMetricWhenGetsTokenWithWebview() {
		doCallRealMethod().when(_metricSenderMock).didInitStart();
		doCallRealMethod().when(_metricSenderMock).didConfigRequestStart();
		doCallRealMethod().when(_metricSenderMock).sdkTokenDidBecomeAvailableWithConfig(false);

		_metricSenderMock.didInitStart();
		_metricSenderMock.didConfigRequestStart();
		_metricSenderMock.sdkTokenDidBecomeAvailableWithConfig(false);
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));

		_metricSenderMock.sdkTokenDidBecomeAvailableWithConfig(false);
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));
	}

	@Test
	public void testDoesNotSendLatencyMetricWhenStartConfigNotCalled() {
		doCallRealMethod().when(_metricSenderMock).didInitStart();
		doCallRealMethod().when(_metricSenderMock).sdkTokenDidBecomeAvailableWithConfig(true);

		_metricSenderMock.didInitStart();
		_metricSenderMock.sdkTokenDidBecomeAvailableWithConfig(true);
		Mockito.verify(_metricSenderMock, Mockito.times(2)).sendMetric(Mockito.any(Metric.class));
	}

	@Test
	public void testDoesNotSendInitMetricWhenInitStartedNotCalled() {
		doCallRealMethod().when(_metricSenderMock).sdkDidInitialize();
		doCallRealMethod().when(_metricSenderMock).sdkInitializeFailed(Mockito.anyString());

		_metricSenderMock.sdkDidInitialize();
		Mockito.verify(_metricSenderMock, never()).sendMetric(Mockito.any(Metric.class));

		_metricSenderMock.sdkInitializeFailed(Mockito.anyString());
		Mockito.verify(_metricSenderMock, never()).sendMetric(Mockito.any(Metric.class));
	}

}