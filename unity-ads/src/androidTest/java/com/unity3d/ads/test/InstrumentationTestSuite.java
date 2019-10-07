package com.unity3d.ads.test;

import com.unity3d.ads.test.instrumentation.services.ads.load.LoadModuleTest;
import com.unity3d.ads.test.instrumentation.services.ads.webplayer.WebPlayerViewCacheTest;
import com.unity3d.ads.test.instrumentation.services.ads.webplayer.WebPlayerViewSettingsCacheTest;
import com.unity3d.ads.test.instrumentation.services.banners.BannerViewCacheTests;
import com.unity3d.ads.test.instrumentation.services.core.configuration.InitializationNotificationCenterTest;
import com.unity3d.ads.properties.AdsPropertiesTests;
import com.unity3d.services.analytics.AcquisitionTypeTest;
import com.unity3d.services.analytics.UnityAnalyticsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	UnityAnalyticsTest.class,
	AcquisitionTypeTest.class,
	AdsPropertiesTests.class,
	InitializationNotificationCenterTest.class,
	LoadModuleTest.class,
//	LoadBridgeTest.class,
	WebPlayerViewSettingsCacheTest.class,
	WebPlayerViewCacheTest.class,
	BannerViewCacheTests.class
})

public class InstrumentationTestSuite {}
