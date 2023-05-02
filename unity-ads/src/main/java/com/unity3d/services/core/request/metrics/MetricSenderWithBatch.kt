package com.unity3d.services.core.request.metrics

import android.text.TextUtils
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.properties.InitializationStatusReader
import java.util.concurrent.LinkedBlockingQueue

class MetricSenderWithBatch(
    private var _original: SDKMetricsSender,
    initializationStatusReader: InitializationStatusReader
) : MetricSenderBase(
    initializationStatusReader
) {
    private val _queue = LinkedBlockingQueue<Metric>()
    fun updateOriginal(metrics: SDKMetricsSender) {
        _original = metrics
    }

    override fun areMetricsEnabledForCurrentSession(): Boolean {
        return _original.areMetricsEnabledForCurrentSession()
    }

    override fun sendEvent(event: String, value: String?, tags: Map<String, String>) {
        if (event.isEmpty()) {
            DeviceLog.debug("Metric event not sent due to being empty: $event")
            return
        }
        sendMetrics(listOf(Metric(event, value, tags)))
    }

    override fun sendMetric(metric: Metric) {
        sendMetrics(listOf(metric))
    }

    @Synchronized
    override fun sendMetrics(metrics: List<Metric>) {
        _queue.addAll(metrics)
        if (!TextUtils.isEmpty(_original.metricEndPoint) && _queue.size > 0) {
            val eventsToSend: MutableList<Metric> = mutableListOf()
            _queue.drainTo(eventsToSend)
            _original.sendMetrics(eventsToSend)
        }
    }

    override val metricEndPoint: String?
        get() = _original.metricEndPoint

    fun sendQueueIfNeeded() {
        sendMetrics(emptyList())
    }
}