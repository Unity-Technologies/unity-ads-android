package com.unity3d.services.core.request.metrics

import com.unity3d.services.core.device.Device
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.properties.Session.Default.id

class MetricsContainer(
    private val metricSampleRate: String,
    private val commonTags: MetricCommonTags,
    private val metrics: List<Metric>,
    private val sTkn: String?
) {
    private val shSid = id
    private val apiLevel = Device.getApiLevel().toString()
    private val deviceModel = Device.getModel()
    private val deviceName = Device.getDevice()
    private val deviceManufacturer =Device.getManufacturer()
    private val gameId = ClientProperties.getGameId()
    fun toMap(): Map<String, Any> {
        val metricsMaps = metrics.map { it.toMap() }

        val result = buildMap {
            put(METRIC_CONTAINER_SAMPLE_RATE, metricSampleRate)
            put(METRICS_CONTAINER, metricsMaps)
            put(METRICS_CONTAINER_TAGS, commonTags.toMap())
            put(METRIC_CONTAINER_SHARED_SESSION_ID, shSid)
            put(METRIC_CONTAINER_API_LEVEL, apiLevel)
            sTkn?.let { put(METRIC_CONTAINER_SESSION_TOKEN, it) }
            deviceModel?.let { put(METRIC_CONTAINER_DEVICE_MODEL, it) }
            deviceName?.let { put(METRIC_CONTAINER_DEVICE_NAME, it) }
            deviceManufacturer?.let { put(METRIC_CONTAINER_DEVICE_MAKE, it) }
            gameId?.let { put(METRIC_CONTAINER_GAME_ID, it) }
        }

        return result
    }

    companion object {
        private const val METRICS_CONTAINER = "m"
        private const val METRICS_CONTAINER_TAGS = "t"
        private const val METRIC_CONTAINER_SAMPLE_RATE = "msr"
        private const val METRIC_CONTAINER_SESSION_TOKEN = "sTkn"
        private const val METRIC_CONTAINER_SHARED_SESSION_ID = "shSid"
        private const val METRIC_CONTAINER_API_LEVEL = "apil"
        private const val METRIC_CONTAINER_DEVICE_MAKE = "deviceMake"
        private const val METRIC_CONTAINER_DEVICE_NAME = "deviceName"
        private const val METRIC_CONTAINER_DEVICE_MODEL = "deviceModel"
        private const val METRIC_CONTAINER_GAME_ID = "gameId"
    }
}