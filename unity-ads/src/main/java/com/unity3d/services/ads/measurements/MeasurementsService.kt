package com.unity3d.services.ads.measurements

import android.adservices.AdServicesState
import android.adservices.measurement.MeasurementManager
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import android.view.InputEvent
import com.unity3d.services.core.device.Device
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import kotlinx.coroutines.asExecutor

@SuppressLint("NewApi", "MissingPermission")
class MeasurementsService(context: Context, private val dispatchers: ISDKDispatchers, private val eventSender: IEventSender) {
    private val measurementManager: MeasurementManager? = getMeasurementManager(context)

    fun checkAvailability() {
        if (Device.getApiLevel() < 33) {
            eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, MeasurementsEvents.NOT_AVAILABLE, MeasurementsErrors.ERROR_API_BELOW_33)
            return
        }

        if (SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) < 4) {
            eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, MeasurementsEvents.NOT_AVAILABLE, MeasurementsErrors.ERROR_EXTENSION_BELOW_4)
            return
        }

        if (measurementManager == null) {
            eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, MeasurementsEvents.NOT_AVAILABLE, MeasurementsErrors.ERROR_MANAGER_NULL)
            return
        }

        if (!AdServicesState.isAdServicesStateEnabled()) {
            eventSender.sendEvent(WebViewEventCategory.MEASUREMENTS, MeasurementsEvents.NOT_AVAILABLE, MeasurementsErrors.ERROR_AD_SERVICES_DISABLED)
            return
        }

        measurementManager.getMeasurementApiStatus(dispatchers.default.asExecutor(), MeasurementsStatusReceiver(eventSender))
    }

    fun registerView(url: String) {
        measurementManager?.registerSource(
            Uri.parse(url),
            null,
            dispatchers.default.asExecutor(),
            MeasurementsReceiver(eventSender, MeasurementsEvents.VIEW_SUCCESSFUL, MeasurementsEvents.VIEW_ERROR)
        )
    }

    fun registerClick(url: String, inputEvent: InputEvent) {
        measurementManager?.registerSource(
            Uri.parse(url),
            inputEvent,
            dispatchers.default.asExecutor(),
            MeasurementsReceiver(eventSender, MeasurementsEvents.CLICK_SUCCESSFUL, MeasurementsEvents.CLICK_ERROR)
        )
    }

    private fun getMeasurementManager(context: Context): MeasurementManager? {
        // accessing MeasurementManager without API level and extension version checks can crash old Android devices
        // also accessing SdkExtensions below API level 30 causes exception so check API level first
        if (Device.getApiLevel() < 33) {
            return null
        }

        if (SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) < 4) {
            return null
        }

        return context.getSystemService(MeasurementManager::class.java)
    }
}
