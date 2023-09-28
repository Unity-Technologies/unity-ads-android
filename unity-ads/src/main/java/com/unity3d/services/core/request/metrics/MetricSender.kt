package com.unity3d.services.core.request.metrics

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.di.IServiceComponent
import com.unity3d.services.core.di.ServiceProvider
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.network.core.HttpClient
import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.RequestType
import com.unity3d.services.core.properties.InitializationStatusReader
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.math.roundToInt

open class MetricSender(
    configuration: Configuration,
    initializationStatusReader: InitializationStatusReader
) : MetricSenderBase(initializationStatusReader), IServiceComponent {
    private val commonTags = MetricCommonTags().also {
        it.updateWithConfig(configuration)
    }
    private val metricSampleRate = configuration.metricSampleRate.roundToInt().toString()
    private val sessionToken = configuration.sessionToken
    private val dispatchers = ServiceProvider.getRegistry().getService<ISDKDispatchers>("", ISDKDispatchers::class)
    private val httpClient = ServiceProvider.getRegistry().getService<HttpClient>("", HttpClient::class)
    private val scope = CoroutineScope(dispatchers.io)

    override val metricEndPoint: String? = configuration.metricsUrl

    override fun sendEvent(event: String, value: String?, tags: Map<String, String>) {
        if (event.isEmpty()) {
            DeviceLog.debug("Metric event not sent due to being null or empty: $event")
            return
        }
        sendMetrics(listOf(Metric(event, value, tags)))
    }

    override fun sendMetric(metric: Metric) {
        sendMetrics(listOf(metric))
    }

    override fun sendMetrics(metrics: List<Metric>) {
        if (metrics.isEmpty()) {
            DeviceLog.debug("Metrics event not send due to being empty")
            return
        }
        if (metricEndPoint.isNullOrBlank()) {
            DeviceLog.debug("Metrics: $metrics was not sent to null or empty endpoint: $metricEndPoint")
            return
        }

        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            DeviceLog.debug("Metric $metrics failed to send with error: $throwable")
        }

        scope.launch(errorHandler) {
            val container = MetricsContainer(metricSampleRate, commonTags, metrics, sessionToken)
            val postBody = JSONObject(container.toMap()).toString()
            val request = HttpRequest(
                baseURL = metricEndPoint.orEmpty(),
                method = RequestType.POST,
                body = postBody,
            )
            val response = httpClient.execute(request)
            val is2XXResponseCode = response.statusCode / 100 == 2
            if (is2XXResponseCode) {
                DeviceLog.debug("Metric $metrics sent to $metricEndPoint")
            } else {
                DeviceLog.debug("Metric $metrics failed to send with response code: ${response.statusCode}")
            }
        }
    }

    fun shutdown() {
        commonTags.shutdown()
    }
}
