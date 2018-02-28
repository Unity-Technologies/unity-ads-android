package com.unity3d.ads.adunit;

public class AdUnitMotionEvent {
	private int _action;
	private boolean _isObscured;
	private int _toolType;
	private int _source;
	private int _deviceId;
	private float _x;
	private float _y;
	private long _eventTime;
	private float _pressure;
	private float _size;

	public AdUnitMotionEvent(int action, boolean isObscured, int toolType, int source, int deviceId, float x, float y, long eventTime, float pressure, float size) {
		_action = action;
		_isObscured = isObscured;
		_toolType = toolType;
		_source = source;
		_deviceId = deviceId;
		_x = x;
		_y = y;
		_eventTime = eventTime;
		_pressure = pressure;
		_size = size;
	}

	public int getAction() {
		return _action;
	}

	public boolean isObscured() {
		return _isObscured;
	}

	public int getToolType() {
		return _toolType;
	}

	public int getSource() {
		return _source;
	}

	public int getDeviceId() {
		return _deviceId;
	}

	public float getX() {
		return _x;
	}

	public float getY() {
		return _y;
	}

	public long getEventTime() {
		return _eventTime;
	}

	public float getPressure() {
		return _pressure;
	}

	public float getSize() {
		return _size;
	}
}
