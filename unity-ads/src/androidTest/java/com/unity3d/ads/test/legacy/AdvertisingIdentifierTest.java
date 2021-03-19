package com.unity3d.ads.test.legacy;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.device.AdvertisingId;

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
    AdvertisingId.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
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
