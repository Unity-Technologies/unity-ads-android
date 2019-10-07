package com.unity3d.ads.test;

import com.unity3d.ads.test.legacy.AdUnitActivityTest;
import com.unity3d.ads.test.legacy.AdvertisingIdentifierTest;
import com.unity3d.ads.test.legacy.BroadcastTest;
import com.unity3d.ads.test.legacy.CacheTest;
import com.unity3d.ads.test.legacy.ClientPropertiesTest;
import com.unity3d.ads.test.legacy.ConnectivityTest;
import com.unity3d.ads.test.legacy.DeviceTest;
import com.unity3d.ads.test.legacy.EnvironmentCheckTest;
import com.unity3d.ads.test.legacy.EventIdTest;
import com.unity3d.ads.test.legacy.InitializeThreadTest;
import com.unity3d.ads.test.legacy.InvocationTest;
import com.unity3d.ads.test.legacy.LifecycleListenerTest;
import com.unity3d.ads.test.legacy.MetaDataTest;
import com.unity3d.ads.test.legacy.NativeCallbackTest;
import com.unity3d.ads.test.legacy.PackageManagerTest;
import com.unity3d.ads.test.legacy.PlacementTest;
import com.unity3d.ads.test.legacy.PreferencesTest;
import com.unity3d.ads.test.legacy.PublicApiTest;
import com.unity3d.ads.test.legacy.RequestTest;
import com.unity3d.ads.test.legacy.SdkPropertiesTest;
import com.unity3d.ads.test.legacy.StorageDiskTest;
import com.unity3d.ads.test.legacy.StorageGeneralTest;
import com.unity3d.ads.test.legacy.StorageMemoryTest;
import com.unity3d.ads.test.legacy.UtilitiesTest;
import com.unity3d.ads.test.legacy.VideoViewTest;
import com.unity3d.ads.test.legacy.VolumeChangeTest;
import com.unity3d.ads.test.legacy.WebPlayerViewTest;
import com.unity3d.ads.test.legacy.WebRequestThreadPoolTest;
import com.unity3d.ads.test.legacy.WebViewAppTest;
import com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest;
import com.unity3d.ads.test.legacy.WebViewBridgeTest;
import com.unity3d.ads.test.legacy.WebViewCallbackTest;

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
	WebPlayerViewTest.class,
	PreferencesTest.class,
	WebRequestThreadPoolTest.class
})

public class LegacyTestSuite {}
