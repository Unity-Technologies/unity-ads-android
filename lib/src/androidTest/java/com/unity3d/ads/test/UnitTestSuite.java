package com.unity3d.ads.test;

import com.unity3d.ads.test.unit.AdUnitActivityTest;
import com.unity3d.ads.test.unit.AdvertisingIdentifierTest;
import com.unity3d.ads.test.unit.BroadcastTest;
import com.unity3d.ads.test.unit.CacheTest;
import com.unity3d.ads.test.unit.ClientPropertiesTest;
import com.unity3d.ads.test.unit.ConnectivityTest;
import com.unity3d.ads.test.unit.DeviceTest;
import com.unity3d.ads.test.unit.EnvironmentCheckTest;
import com.unity3d.ads.test.unit.EventIdTest;
import com.unity3d.ads.test.unit.InitializeThreadTest;
import com.unity3d.ads.test.unit.InvocationTest;
import com.unity3d.ads.test.unit.MetaDataTest;
import com.unity3d.ads.test.unit.NativeCallbackTest;
import com.unity3d.ads.test.unit.PackageManagerTest;
import com.unity3d.ads.test.unit.PlacementTest;
import com.unity3d.ads.test.unit.PublicApiTest;
import com.unity3d.ads.test.unit.RequestTest;
import com.unity3d.ads.test.unit.SdkPropertiesTest;
import com.unity3d.ads.test.unit.StorageDiskTest;
import com.unity3d.ads.test.unit.StorageGeneralTest;
import com.unity3d.ads.test.unit.StorageMemoryTest;
import com.unity3d.ads.test.unit.VideoViewTest;
import com.unity3d.ads.test.unit.WebRequestTest;
import com.unity3d.ads.test.unit.WebViewAppTest;
import com.unity3d.ads.test.unit.WebViewBridgeInterfaceTest;
import com.unity3d.ads.test.unit.WebViewBridgeTest;
import com.unity3d.ads.test.unit.WebViewCallbackTest;

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
  WebRequestTest.class,
  WebViewAppTest.class,
  WebViewBridgeInterfaceTest.class,
  WebViewBridgeTest.class,
  WebViewCallbackTest.class
})
public class UnitTestSuite {
}
