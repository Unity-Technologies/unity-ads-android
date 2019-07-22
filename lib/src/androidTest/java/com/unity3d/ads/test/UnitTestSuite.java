package com.unity3d.ads.test;

import com.unity3d.ads.test.unit.*;
import com.unity3d.ads.test.unit.services.ads.load.LoadBridgeTest;
import com.unity3d.ads.test.unit.services.ads.load.LoadModuleTest;
import com.unity3d.ads.test.unit.services.core.configuration.InitializationNotificationCenterTest;
import com.unity3d.services.ads.properties.AdsPropertiesTests;
import com.unity3d.services.analytics.UnityAnalyticsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	AdUnitActivityTest.class,
	AdvertisingIdentifierTest.class,
	BroadcastTest.class,
	CacheTest.class,
	ClientPropertiesTest.class,
	ConnectivityTest.class,
	DeviceTest.class,
	EventIdTest.class,
	EnvironmentCheckTest.class,
	EventIdTest.class,
	InitializeThreadTest.class,
	InvocationTest.class,
	MetaDataTest.class,
	NativeCallbackTest.class,
	PackageManagerTest.class,
	PlacementTest.class,
	PublicApiTest.class,
	RequestTest.class,
	SdkPropertiesTest.class,
	StorageDiskTest.class,
	StorageGeneralTest.class,
	StorageMemoryTest.class,
	VideoViewTest.class,
	WebViewAppTest.class,
	WebViewBridgeInterfaceTest.class,
	WebViewBridgeTest.class,
	WebViewCallbackTest.class,
	LifecycleListenerTest.class,
	VolumeChangeTest.class,
	UtilitiesTest.class,
	WebPlayerTest.class,
	PreferencesTest.class,
	WebRequestThreadPoolTest.class,
	UnityAnalyticsTest.class,
	AdsPropertiesTests.class,
	InitializationNotificationCenterTest.class,
	LoadModuleTest.class,
	LoadBridgeTest.class
})

public class UnitTestSuite {}
