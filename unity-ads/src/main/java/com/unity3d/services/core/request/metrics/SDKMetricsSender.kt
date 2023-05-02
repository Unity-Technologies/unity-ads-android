package com.unity3d.services.core.request.metrics

interface SDKMetricsSender {
    fun areMetricsEnabledForCurrentSession(): Boolean
    fun sendEvent(event: String) = sendEvent(event, null)
    fun sendEvent(event: String, value: String? = null, tags: Map<String, String> = emptyMap())
    fun sendMetric(metric: Metric)
    fun sendMetrics(metrics: List<Metric>)
    fun sendMetricWithInitState(metric: Metric)
    val metricEndPoint: String?
}