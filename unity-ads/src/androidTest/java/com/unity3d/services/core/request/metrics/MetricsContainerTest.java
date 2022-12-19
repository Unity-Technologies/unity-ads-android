package com.unity3d.services.core.request.metrics;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
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

}