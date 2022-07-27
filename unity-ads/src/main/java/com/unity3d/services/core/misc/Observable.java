package com.unity3d.services.core.misc;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable<T> {
	private final List<IObserver<T>> _observers;

	public Observable() {
		_observers = new ArrayList<>();
	}

	public synchronized void registerObserver(IObserver<T> observer) {
		if (_observers.contains(observer)) return;
		_observers.add(observer);
	}

	public synchronized void unregisterObserver(IObserver<T> observer) {
		if (_observers.contains(observer)) {
			_observers.remove(observer);
		}
	}

	protected synchronized void notifyObservers(T value) {
		for (IObserver<T> observer : _observers) {
			observer.updated(value);
		}
	}
}
