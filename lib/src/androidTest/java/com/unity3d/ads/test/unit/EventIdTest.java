package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.device.Device;
import com.unity3d.ads.properties.ClientProperties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EventIdTest {
	@BeforeClass
	public static void prepareTests() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@Test
	public void eventIdBasicTest() {
		String eventId = Device.getUniqueEventId();

		assertNotEquals("Event ID must never be null", null, eventId);
		assertEquals("Event ID needs to be at least 128-bit id", eventId.length() >= 32, true);
	}

	@Test
	public void eventIdStressTest() {
		HashSet<String> oldIds = new HashSet<String>();

		for(int i = 0; i < 1024; i++) {
			String eventId = Device.getUniqueEventId();

			assertNotEquals("Event ID must never be null", null, eventId);
			assertEquals("There must never be duplicate event ids", oldIds.contains(eventId), false);

			oldIds.add(eventId);
		}
	}
}