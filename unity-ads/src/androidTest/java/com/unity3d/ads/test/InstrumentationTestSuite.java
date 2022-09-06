package com.unity3d.ads.test;

import com.unity3d.ads.test.instrumentation.services.ads.gmascar.GmaScarTestSuite;
import com.unity3d.ads.test.instrumentation.services.ads.operation.AdOperationTests;
import com.unity3d.ads.test.instrumentation.services.ads.operation.LoadModuleDecoratorInitializationBufferTests;
import com.unity3d.ads.test.instrumentation.services.ads.operation.LoadModuleDecoratorTests;
import com.unity3d.ads.test.instrumentation.services.ads.operation.LoadModuleDecoratorTimeoutTests;
import com.unity3d.ads.test.instrumentation.services.ads.operation.LoadModuleTests;
import com.unity3d.ads.test.instrumentation.services.ads.operation.LoadOperationTests;
import com.unity3d.ads.test.instrumentation.services.ads.operation.ShowModuleTests;
import com.unity3d.ads.test.instrumentation.services.ads.webplayer.WebPlayerViewCacheTest;
import com.unity3d.ads.test.instrumentation.services.ads.webplayer.WebPlayerViewSettingsCacheTest;
import com.unity3d.ads.test.instrumentation.services.banners.BannerViewCacheTests;
import com.unity3d.ads.test.instrumentation.services.core.configuration.ExperimentObjectTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.ExperimentObjectsTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.ExperimentsReaderTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.ExperimentsTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.InitializationNotificationCenterTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.PrivacyConfigStatusTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.PrivacyConfigStorageTest;
import com.unity3d.ads.test.instrumentation.services.core.configuration.PrivacyConfigTest;
import com.unity3d.ads.test.instrumentation.services.core.device.AsyncTokenStorageTest;
import com.unity3d.ads.test.instrumentation.services.core.device.DeviceInfoReaderCompressorTest;
import com.unity3d.ads.test.instrumentation.services.core.device.DeviceInfoReaderFilterProviderTest;
import com.unity3d.ads.test.instrumentation.services.core.device.DeviceInfoReaderWithFilterTest;
import com.unity3d.ads.test.instrumentation.services.core.device.DeviceInfoReaderWithPIITest;
import com.unity3d.ads.test.instrumentation.services.core.device.DeviceInfoReaderWithStorageInfoTest;
import com.unity3d.ads.test.instrumentation.services.core.device.NativeTokenGeneratorTest;
import com.unity3d.ads.test.instrumentation.services.core.device.pii.PiiDataSelectorTest;
import com.unity3d.ads.test.instrumentation.services.core.device.pii.PiiDecisionDataTest;
import com.unity3d.ads.test.instrumentation.services.core.device.pii.PiiTrackingStatusReaderTest;
import com.unity3d.ads.test.instrumentation.services.core.misc.IntervalTimerTest;
import com.unity3d.ads.test.instrumentation.services.core.misc.JsonFlattenerTest;
import com.unity3d.ads.test.instrumentation.services.core.webview.WebViewUrlBuilderTest;
import com.unity3d.ads.test.instrumentation.services.core.webview.bridge.WebViewBridgeSharedObjectTests;
import com.unity3d.ads.test.instrumentation.services.core.webview.bridge.invocation.WebViewBridgeInvocationRunnableTests;
import com.unity3d.ads.test.instrumentation.services.core.webview.bridge.invocation.WebViewBridgeInvocationTests;
import com.unity3d.ads.test.instrumentation.services.store.gpbl.bridges.PurchasesResultBridgeTest;
import com.unity3d.ads.test.legacy.ConfigurationTest;
import com.unity3d.services.analytics.AcquisitionTypeTest;
import com.unity3d.services.analytics.UnityAnalyticsTest;
import com.unity3d.services.core.request.metrics.MetricCommonTagsTest;
import com.unity3d.services.core.request.metrics.MetricsContainerTest;
import com.unity3d.services.core.timer.BaseTimerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	UnityAnalyticsTest.class,
	AcquisitionTypeTest.class,
	InitializationNotificationCenterTest.class,
	ExperimentObjectTest.class,
	ExperimentObjectsTest.class,
	ExperimentsTest.class,
	ExperimentsReaderTest.class,
	PrivacyConfigStatusTest.class,
	PrivacyConfigStorageTest.class,
	PrivacyConfigTest.class,
	WebViewUrlBuilderTest.class,
	WebPlayerViewSettingsCacheTest.class,
	WebPlayerViewCacheTest.class,
	BannerViewCacheTests.class,
	WebViewBridgeSharedObjectTests.class,
	WebViewBridgeInvocationTests.class,
	WebViewBridgeInvocationRunnableTests.class,
	LoadOperationTests.class,
	LoadModuleTests.class,
	LoadModuleDecoratorTimeoutTests.class,
	LoadModuleDecoratorTests.class,
	LoadModuleDecoratorInitializationBufferTests.class,
	AdOperationTests.class,
	ShowModuleTests.class,
	ConfigurationTest.class,
	GmaScarTestSuite.class,
	AsyncTokenStorageTest.class,
	NativeTokenGeneratorTest.class,
	PiiTrackingStatusReaderTest.class,
	PiiDecisionDataTest.class,
	PiiDataSelectorTest.class,
	DeviceInfoReaderCompressorTest.class,
	DeviceInfoReaderFilterProviderTest.class,
	DeviceInfoReaderWithStorageInfoTest.class,
	DeviceInfoReaderWithPIITest.class,
	DeviceInfoReaderWithFilterTest.class,
	JsonFlattenerTest.class,
	PurchasesResultBridgeTest.class,
	MetricCommonTagsTest.class,
	MetricsContainerTest.class,
	IntervalTimerTest.class,
	BaseTimerTest.class
})

public class InstrumentationTestSuite {}
