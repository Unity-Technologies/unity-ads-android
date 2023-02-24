package com.unity3d.services.core.request.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Map;

public class MetricsContainerTest {

	@Test
	public void testMSRinRoot() {
		MetricCommonTags commonTags = Mockito.mock(MetricCommonTags.class);
		Map<String, Object> currentTags;

		MetricsContainer container = new MetricsContainer("50", commonTags, new ArrayList<Metric>(), "token");
		currentTags = container.asMap();

		assertEquals("Incorrect metricSampleRate value", "50", currentTags.get("msr"));
	}

	@Test
	public void testSTknInRoot() {
		MetricCommonTags commonTags = Mockito.mock(MetricCommonTags.class);
		Map<String, Object> currentTags;

		MetricsContainer container = new MetricsContainer("50", commonTags, new ArrayList<Metric>(), "token");
		currentTags = container.asMap();

		assertEquals("Incorrect session token value", "token", currentTags.get("sTkn"));
	}

	@Test
	public void testShSidInRoot() {
		MetricCommonTags commonTags = Mockito.mock(MetricCommonTags.class);
		Map<String, Object> currentTags;

		MetricsContainer container = new MetricsContainer("50", commonTags, new ArrayList<Metric>(), "token");
		currentTags = container.asMap();

		assertNotNull("Shared session id missing value", currentTags.get("shSid"));
	}

}