package com.unity3d.services.core.misc;

public interface IObserver<T> {
	void updated(T value);
}
