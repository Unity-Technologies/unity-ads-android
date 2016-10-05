package com.wds.ads.test;

import com.wds.ads.test.unit.AdUnitActivityTest;
import com.wds.ads.test.unit.AdvertisingIdentifierTest;
import com.wds.ads.test.unit.BroadcastTest;
import com.wds.ads.test.unit.CacheTest;
import com.wds.ads.test.unit.ClientPropertiesTest;
import com.wds.ads.test.unit.ConnectivityTest;
import com.wds.ads.test.unit.DeviceTest;
import com.wds.ads.test.unit.EnvironmentCheckTest;
import com.wds.ads.test.unit.EventIdTest;
import com.wds.ads.test.unit.InitializeThreadTest;
import com.wds.ads.test.unit.InvocationTest;
import com.wds.ads.test.unit.MetaDataTest;
import com.wds.ads.test.unit.NativeCallbackTest;
import com.wds.ads.test.unit.PackageManagerTest;
import com.wds.ads.test.unit.PlacementTest;
import com.wds.ads.test.unit.PublicApiTest;
import com.wds.ads.test.unit.RequestTest;
import com.wds.ads.test.unit.SdkPropertiesTest;
import com.wds.ads.test.unit.StorageDiskTest;
import com.wds.ads.test.unit.StorageGeneralTest;
import com.wds.ads.test.unit.StorageMemoryTest;
import com.wds.ads.test.unit.VideoViewTest;
import com.wds.ads.test.unit.WebRequestTest;
import com.wds.ads.test.unit.WebViewAppTest;
import com.wds.ads.test.unit.WebViewBridgeInterfaceTest;
import com.wds.ads.test.unit.WebViewBridgeTest;
import com.wds.ads.test.unit.WebViewCallbackTest;

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
