package com.unity3d.services.ads.topics

import android.adservices.AdServicesState
import android.adservices.topics.GetTopicsRequest
import android.adservices.topics.TopicsManager
import android.annotation.SuppressLint
import android.content.Context
import android.os.ext.SdkExtensions
import com.unity3d.services.core.device.Device
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import kotlinx.coroutines.asExecutor

@SuppressLint("NewApi", "MissingPermission")
class TopicsService(context: Context, private val dispatchers: ISDKDispatchers, private val eventSender: IEventSender) {
    private val topicsManager: TopicsManager? = getTopicsManager(context)

    fun checkAvailability(): TopicsStatus {
        if (Device.getApiLevel() < 33) {
            return TopicsStatus.ERROR_API_BELOW_33
        }

        if (SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) < 4) {
            return TopicsStatus.ERROR_EXTENSION_BELOW_4
        }

        if (topicsManager == null) {
            return TopicsStatus.ERROR_TOPICSMANAGER_NULL
        }

        if (!AdServicesState.isAdServicesStateEnabled()) {
            return TopicsStatus.ERROR_AD_SERVICES_DISABLED
        }

        return TopicsStatus.TOPICS_AVAILABLE
    }

    fun getTopics(adsSdkName: String, shouldRecordObservation: Boolean) {
        val callback = TopicsReceiver(eventSender)
        val topicsRequest = GetTopicsRequest.Builder().setAdsSdkName(adsSdkName).setShouldRecordObservation(shouldRecordObservation).build()
        try {
            topicsManager?.getTopics(topicsRequest, dispatchers.default.asExecutor(), callback)
        } catch (error: Exception) {
            eventSender.sendEvent(WebViewEventCategory.TOPICS, TopicsEvents.NOT_AVAILABLE, TopicsErrors.ERROR_EXCEPTION, error.toString())
            DeviceLog.debug("Failed to get topics with error: $error")
        }
    }

    private fun getTopicsManager(context: Context): TopicsManager? {
        // accessing TopicsManager without API level and extension version checks can crash old Android devices
        // also accessing SdkExtensions below API level 30 causes exception so check API level first
        if (Device.getApiLevel() < 33) {
            return null
        }

        if (SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) < 4) {
            return null
        }

        return context.getSystemService(TopicsManager::class.java)
    }
}
