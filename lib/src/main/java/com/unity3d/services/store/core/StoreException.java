package com.unity3d.services.store.core;

public class StoreException extends Exception {
	private int _resultCode;

	public StoreException() {
		super("Unknown store exception");
		_resultCode = -1;
	}

	public StoreException(int resultCode) {
		super("Store exception with result code " + resultCode);
		_resultCode = resultCode;
	}

	public int getResultCode() {
		return _resultCode;
	}
}
