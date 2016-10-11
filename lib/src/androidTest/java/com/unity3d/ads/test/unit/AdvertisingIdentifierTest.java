package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.device.AdvertisingId;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AdvertisingIdentifierTest {

  @BeforeClass
  public static void setupAdvertisingIdentifier() {
    AdvertisingId.init(InstrumentationRegistry.getTargetContext());
  }

  @Test
  @Ignore
  public void testAdvertisingIdentifier() {
    assertNotNull(AdvertisingId.getAdvertisingTrackingId());
  }

  @Test
  @Ignore
  public void testLimitedAdTracking() {
    assertEquals(false, AdvertisingId.getLimitedAdTracking());
  }
}
