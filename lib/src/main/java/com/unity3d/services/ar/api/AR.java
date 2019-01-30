package com.unity3d.services.ar.api;

import android.content.Context;
import com.unity3d.services.ads.adunit.AdUnitActivity;
import com.unity3d.services.ads.adunit.IAdUnitViewHandler;
import com.unity3d.services.ads.api.AdUnit;
import com.unity3d.services.ar.ARCheck;
import com.unity3d.services.ar.ARError;
import com.unity3d.services.ar.ARUtils;
import com.unity3d.services.ar.view.ARView;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AR {
	@WebViewExposed
	public static void isARSupported(final WebViewCallback callback) {
		if (!ARCheck.isFrameworkPresent()) {
			callback.invoke(false, false);
			return;
		}

		Context context = ClientProperties.getApplicationContext();
		if (context != null) {
			int result = ARUtils.isSupported(context);
			boolean isTransient = (result & ARUtils.AR_CHECK_TRANSIENT) != 0;
			boolean isSupported = (result & ARUtils.AR_CHECK_SUPPORTED) != 0;
			callback.invoke(isTransient, isSupported);
		}
	}

	@WebViewExposed
	public static void showCameraFeed(WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().setShowCameraFrame(true);
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void hideCameraFeed(WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().setShowCameraFrame(false);
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void restartSession(final JSONObject properties, final WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			try {
				AR.getARView().restartSession(properties);
				callback.invoke();
			} catch (JSONException e) {
				callback.error(ARError.ARCONFIG_INVALID);
			}
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void setDepthFar(Double far, WebViewCallback callback) {
		if (far == null) {
			callback.error(ARError.INVALID_VALUE);
			return;
		}

		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().setArFar(far.floatValue());
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void setDepthNear(Double near, WebViewCallback callback) {
		if (near == null) {
			callback.error(ARError.INVALID_VALUE);
			return;
		}

		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().setArNear(near.floatValue());
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void addAnchor(String identifier, String matrix, WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().addAnchor(identifier, matrix);
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void removeAnchor(String identifier, WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().removeAnchor(identifier);
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void advanceFrame(WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().setDrawNextCameraFrame();
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void swapBuffers(WebViewCallback callback) {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			AR.getARView().swapBuffers();
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	@WebViewExposed
	public static void setViewRenderMode(Integer mode, WebViewCallback callback) {
		final AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null && AR.getARView() != null) {
			try {
				AR.getARView().setRenderMode(mode);
			} catch (IllegalArgumentException e) {
				callback.error(ARError.INVALID_VALUE);
				return;
			}
			callback.invoke();
		} else {
			callback.error(ARError.ARVIEW_NULL);
		}
	}

	// Android doesn't support video format selection at the moment and it's not possible to get the
	// image size before session starts. This is here for API compatibility with iOS.
	@WebViewExposed
	public static void getSupportedVideoFormats(String ignored, WebViewCallback callback) {
		JSONArray supportedFormats = new JSONArray();
		callback.invoke(supportedFormats.toString());
	}

	@WebViewExposed
	public static void getAndroidConfigEnums(WebViewCallback callback) {
		if (!ARCheck.isFrameworkPresent()) {
			callback.error(ARError.AR_NOT_SUPPORTED);
			return;
		}

		callback.invoke(ARUtils.getConfigEnums());
	}

	private static ARView getARView() {
		AdUnitActivity activity = AdUnit.getAdUnitActivity();
		if (activity != null) {
			IAdUnitViewHandler handler = activity.getViewHandler("arview");
			if (handler != null) {
				return (ARView)handler.getView();
			}
		}

		return null;
	}
}
