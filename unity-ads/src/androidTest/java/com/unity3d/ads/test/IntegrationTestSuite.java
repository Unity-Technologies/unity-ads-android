package com.unity3d.ads.test;

import com.unity3d.ads.test.integration.banner.BannerIntegrationTest;
import com.unity3d.ads.test.integration.SDKMetricsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	BannerIntegrationTest.class,
	SDKMetricsTest.class
})
public class IntegrationTestSuite {
}
