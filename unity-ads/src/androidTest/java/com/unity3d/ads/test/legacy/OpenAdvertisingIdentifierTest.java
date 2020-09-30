package com.unity3d.ads.test.legacy;

import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.services.core.device.OpenAdvertisingId;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class OpenAdvertisingIdentifierTest {
	@BeforeClass
	public static void setupOpenAdvertisingIdentifier() {
		OpenAdvertisingId.init(InstrumentationRegistry.getTargetContext());
	}

	@Test
	@Ignore
	public void testOpenAdvertisingIdentifier() {
		if(Build.MANUFACTURER.toUpperCase().equals("HUAWEI")) {
			assertNotNull(OpenAdvertisingId.getOpenAdvertisingTrackingId());
		} else {
			assertNull(OpenAdvertisingId.getOpenAdvertisingTrackingId());
		}
	}

	@Test
	@Ignore
	public void testLimitedOpenAdTracking() {
		assertEquals(false, OpenAdvertisingId.getLimitedOpenAdTracking());
	}
}
