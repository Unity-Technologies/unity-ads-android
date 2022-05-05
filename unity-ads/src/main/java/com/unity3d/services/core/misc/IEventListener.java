package com.unity3d.services.core.misc;

public interface IEventListener<T> {
	void onNextEvent(T nextEvent);
}
