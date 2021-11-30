package com.unity3d.services.core.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Abstract class used to enable proxy invocation callbacks to any methods called reflectively.
 */
public abstract class GenericListenerProxy implements InvocationHandler {

	public abstract Class<?> getProxyClass() throws ClassNotFoundException;

	public Object getProxyInstance() throws ClassNotFoundException {
		return Proxy.newProxyInstance(getProxyClass().getClassLoader(), new Class<?>[]{getProxyClass()}, this);
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		// Fallback to just calling method in case proxy method is from Object (for example equals, etc.)
		return m.invoke(this, args);
	}
}
