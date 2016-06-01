package com.unity3d.ads.test;

import com.unity3d.ads.test.unit.*;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  AdUnitActivityTest.class,
  AdvertisingIdentifierTest.class,
  InvocationTest.class,
  CacheTest.class,
  ClientPropertiesTest.class,
  DeviceTest.class,
  EventIdTest.class,
  EnvironmentCheckTest.class,
  InitializeThreadTest.class,
  NativeCallbackTest.class,
  PackageManagerTest.class,
  PublicApiTest.class,
  SdkPropertiesTest.class,
  StorageDiskTest.class,
  StorageGeneralTest.class,
  StorageMemoryTest.class,
  RequestTest.class,
  VideoViewTest.class,
  WebRequestTest.class,
  WebViewAppTest.class,
  WebViewBridgeInterfaceTest.class,
  WebViewBridgeTest.class,
  WebViewCallbackTest.class,
  PlacementTest.class,
  BroadcastTest.class
})
public class UnitTestSuite {}
