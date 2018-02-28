package com.unity3d.ads.test.unit;

import android.hardware.SensorManager;
import android.os.ConditionVariable;

import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.sensorinfo.SensorInfoListener;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SensorInfoTest extends AdUnitActivityTestBaseClass {

	@Before
	public void setup() {
		ClientProperties.setApplicationContext(this.getInstrumentation().getContext());
	}

	@After
	public void teardown() {
		SensorInfoListener.stopAccelerometerListener();
	}

	@Test
	public void testStartAccelerometerListener() {
		assertTrue("Accelerometer should be started", SensorInfoListener.startAccelerometerListener(SensorManager.SENSOR_DELAY_NORMAL));

		assertTrue("Accelerometer should be active", SensorInfoListener.isAccelerometerListenerActive());

	}

	@Test
	public void testStopAccelerometerListener() {
		assertTrue("Accelerometer should be started", SensorInfoListener.startAccelerometerListener(SensorManager.SENSOR_DELAY_NORMAL));

		assertTrue("Accelerometer should be active", SensorInfoListener.isAccelerometerListenerActive());

		SensorInfoListener.stopAccelerometerListener();

		assertFalse("Accelerometer shouldn't be active", SensorInfoListener.isAccelerometerListenerActive());
	}

	@Test
	public void testGetAccelerometerData() throws Exception {
		SensorInfoListener.startAccelerometerListener(SensorManager.SENSOR_DELAY_NORMAL);

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		JSONObject accelerometerData = SensorInfoListener.getAccelerometerData();

		assertNotNull("Accelerometer shouldn't be null", accelerometerData);

		double x = accelerometerData.getDouble("x");
		double y = accelerometerData.getDouble("y");
		double z = accelerometerData.getDouble("z");

		assertTrue(x != 0);
		assertTrue(y != 0);
		assertTrue(z != 0);
	}

}
