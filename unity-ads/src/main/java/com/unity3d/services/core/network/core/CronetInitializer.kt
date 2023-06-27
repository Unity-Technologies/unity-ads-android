package com.unity3d.services.core.network.core

import android.content.Context
import androidx.startup.Initializer
import com.google.android.gms.net.CronetProviderInstaller
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.request.metrics.Metric
import com.unity3d.services.core.request.metrics.SDKMetricsSender
import java.util.concurrent.TimeUnit

/**
 * Cronet initializer via Google Play Services
 */
class CronetInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val startTime = System.nanoTime()
        CronetProviderInstaller.installProvider(context).addOnCompleteListener {
            sendDuration(startTime, it.isSuccessful)
        }
    }

    private fun sendDuration(startTime: Long, success: Boolean) {
        val sdkMetricsSender = Utilities.getService<SDKMetricsSender>(
            SDKMetricsSender::class.java
        )
        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        val metric = if (success) {
            Metric("native_cronet_play_services_success", duration)
        } else {
            Metric("native_cronet_play_services_failure", duration)
        }
        sdkMetricsSender.sendMetric(metric)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}