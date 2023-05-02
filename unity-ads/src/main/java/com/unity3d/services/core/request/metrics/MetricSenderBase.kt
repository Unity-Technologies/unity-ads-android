package com.unity3d.services.core.request.metrics

import com.unity3d.services.core.properties.InitializationStatusReader
import com.unity3d.services.core.properties.SdkProperties

abstract class MetricSenderBase(private val _initStatusReader: InitializationStatusReader) :
    SDKMetricsSender {
    override fun sendMetricWithInitState(metric: Metric) {
        val extraTags = mapOf(AdOperationMetric.INIT_STATE to
            _initStatusReader.getInitializationStateString(SdkProperties.getCurrentInitializationState()))
        val updatedMetric = metric.copy(tags = metric.tags + extraTags)
        sendMetric(updatedMetric)
    }
}