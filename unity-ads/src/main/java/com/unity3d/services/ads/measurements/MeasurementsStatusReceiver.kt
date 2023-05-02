package com.unity3d.services.ads.measurements

import android.annotation.SuppressLint
import android.os.OutcomeReceiver
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender

@SuppressLint("NewApi", "MissingPermission")
class MeasurementsStatusReceiver(private val eventSender: IEventSender) : OutcomeReceiver<Int, Exception> {
    override fun onResult(status: Int) {
        eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, MeasurementsEvents.AVAILABLE, status)
    }

    override fun onError(error: Exception) {
        eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, MeasurementsEvents.NOT_AVAILABLE, MeasurementsErrors.ERROR_EXCEPTION, error.toString())
    }
}
