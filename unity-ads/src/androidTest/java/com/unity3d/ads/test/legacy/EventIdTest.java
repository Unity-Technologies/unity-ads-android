package com.unity3d.ads.test.legacy;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.properties.ClientProperties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EventIdTest {
	@BeforeClass
	public static void prepareTests() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
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