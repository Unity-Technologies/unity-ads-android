package com.unity3d.ads.sensorinfo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorInfoListener implements SensorEventListener{

	private static SensorInfoListener _accelerometerListener = null;

	private static Sensor _accelerometerSensor = null;

	private static SensorEvent _latestAccelerometerEvent = null;

	public static boolean startAccelerometerListener(final int sensorDelay) {
		if(_accelerometerListener == null) {
			_accelerometerListener = new SensorInfoListener();
		}

		SensorManager sensorManager = (SensorManager) ClientProperties.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

		_accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		 return sensorManager.registerListener(_accelerometerListener, _accelerometerSensor, sensorDelay);
	}

	public static void stopAccelerometerListener() {
		if(_accelerometerListener != null) {
			SensorManager sensorManager = (SensorManager) ClientProperties.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
			sensorManager.unregisterListener(_accelerometerListener);
			_accelerometerListener = null;
		}
	}

	public static boolean isAccelerometerListenerActive() {
		return _accelerometerListener != null;
	}

	public static JSONObject getAccelerometerData() {
		JSONObject accelerometerData = null;
		if(_latestAccelerometerEvent != null) {
			accelerometerData = new JSONObject();
			try {
				accelerometerData.put("x", _latestAccelerometerEvent.values[0]);
				accelerometerData.put("y", _latestAccelerometerEvent.values[1]);
				accelerometerData.put("z", _latestAccelerometerEvent.values[2]);
			} catch (JSONException e) {
				DeviceLog.exception("JSON error while constructing accelerometer data", e);
			}
		}
		return accelerometerData;
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			_latestAccelerometerEvent = sensorEvent;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}
