package com.unity3d.services.store.gpbl.bridges;

import com.unity3d.services.core.reflection.GenericBridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkuDetailsParamsBridge extends GenericBridge {
	private final Object _skuDetailsParamsInternalInstance;

	private static final String newBuilderMethodName = "newBuilder";
	private static final Map<String, Class<?>[]> staticMethods = new HashMap<String, Class<?>[]>() {{
		put(newBuilderMethodName, new Class[]{});
	}};

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.SkuDetailsParams";
	}

	public SkuDetailsParamsBridge(Object skuDetailsParamsInternalInstance) {
		super(new HashMap<String, Class[]>(){{
			put(newBuilderMethodName, new Class[]{});
		}});
		_skuDetailsParamsInternalInstance = skuDetailsParamsInternalInstance;
	}

	public Object getInternalInstance() {
		return _skuDetailsParamsInternalInstance;
	}

	public static SkuDetailsParamsBridge.BuilderBridge newBuilder() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
		Object skuDetailsParamsBuilderBridgeInternalInstance = callNonVoidStaticMethod(newBuilderMethodName);
		return new SkuDetailsParamsBridge.BuilderBridge(skuDetailsParamsBuilderBridgeInternalInstance);
	}

	public static Object callNonVoidStaticMethod(String methodName, Object... parameters) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Method method = getClassForBridge().getMethod(methodName, staticMethods.get(methodName));
		return method.invoke(null, parameters);
	}

	public static Class<?> getClassForBridge() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.SkuDetailsParams");
	}

	public static class BuilderBridge extends GenericBridge {
		private static final String buildMethodName = "build";
		private static final String setSkusListMethodName = "setSkusList";
		private static final String setTypeMethodName = "setType";

		private Object _skuDetailsParamsBuilderInternalInstance;

		public BuilderBridge(Object skuDetailsParamsBuilderInternalInstance) {
			super(new HashMap<String, Class[]>(){{
				put(buildMethodName, new Class[]{});
				put(setSkusListMethodName, new Class[]{List.class});
				put(setTypeMethodName, new Class[]{String.class});
			}});
			_skuDetailsParamsBuilderInternalInstance = skuDetailsParamsBuilderInternalInstance;
		}

		@Override
		protected String getClassName() {
			return "com.android.billingclient.api.SkuDetailsParams$Builder";
		}

		public BuilderBridge setSkuList(List<String> skusList) {
			_skuDetailsParamsBuilderInternalInstance = callNonVoidMethod(setSkusListMethodName, _skuDetailsParamsBuilderInternalInstance, skusList);
			return this;
		}

		public BuilderBridge setType(String skuType) {
			_skuDetailsParamsBuilderInternalInstance = callNonVoidMethod(setTypeMethodName, _skuDetailsParamsBuilderInternalInstance, skuType);
			return this;
		}

		public SkuDetailsParamsBridge build() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
			return new SkuDetailsParamsBridge(callNonVoidMethod(buildMethodName, _skuDetailsParamsBuilderInternalInstance));
		}
	}
}
