package com.unity3d.services.ads.api;

import android.view.InputEvent;

import com.unity3d.services.ads.measurements.MeasurementsErrors;
import com.unity3d.services.ads.measurements.MeasurementsService;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Measurements {
	private static final MeasurementsService measurementsService = Utilities.getService(MeasurementsService.class);

	@WebViewExposed
	public static void checkAvailability(WebViewCallback callback) {
		measurementsService.checkAvailability();
		callback.invoke();
	}

	@WebViewExposed
	public static void registerView(final String url, WebViewCallback callback) {
		measurementsService.registerView(url);
		callback.invoke();
	}

	@WebViewExposed
	public static void registerClick(final String url, WebViewCallback callback) {
		if (AdUnit.getAdUnitActivity() == null) {
			callback.error(MeasurementsErrors.ERROR_AD_UNIT_NULL);
			return;
		}

		if (AdUnit.getAdUnitActivity().getLayout() == null) {
			callback.error(MeasurementsErrors.ERROR_LAYOUT_NULL);
			return;
		}

		InputEvent lastInputEvent = AdUnit.getAdUnitActivity().getLayout().getLastInputEvent();
		if (lastInputEvent == null) {
			callback.error(MeasurementsErrors.ERROR_LAST_INPUT_EVENT_NULL);
			return;
		}

		measurementsService.registerClick(url, lastInputEvent);
		callback.invoke();
	}
}
