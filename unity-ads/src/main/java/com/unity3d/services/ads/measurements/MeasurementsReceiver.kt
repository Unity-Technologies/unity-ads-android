package com.unity3d.services.ads.measurements

import android.annotation.SuppressLint
import android.os.OutcomeReceiver
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender

@SuppressLint("NewApi", "MissingPermission")
class MeasurementsReceiver(
    private val eventSender: IEventSender,
    private val successEvent: MeasurementsEvents,
    private val errorEvent: MeasurementsEvents,
): OutcomeReceiver<Any, Exception> {
    override fun onResult(p0: Any) {
        eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, successEvent)
    }

    override fun onError(error: Exception) {
        eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, errorEvent, error.toString())
    }
}
