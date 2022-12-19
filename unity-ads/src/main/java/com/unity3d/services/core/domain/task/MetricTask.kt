package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.request.metrics.ISDKMetrics
import com.unity3d.services.core.request.metrics.Metric
import com.unity3d.services.core.request.metrics.SDKMetrics
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

/**
 * Intercepts a [BaseTask] and captures its [duration] in milliseconds
 * Once completed  only if a metric name is set via {@link #getMetricName()} the metric is sent
 */
abstract class MetricTask<in P : BaseParams, R> : BaseTask<P, R> {

    var duration: Long = 0L
    var taskStatus: String = "unknown"

    override suspend fun invoke(params: P): R {
        var result: R
        duration = TimeUnit.NANOSECONDS.toMillis(
            measureNanoTime {
                result = super.invoke(params)
            })
        captureMetric(result)
        return result
    }

    private fun captureMetric(result: R) {
        taskStatus = if (result is Result<*>) {
            if (result.isSuccess) "success" else "failure"
        } else {
            "success"
        }
        sendMetric()
    }

    /**
     * Function to override to assign a name to the metric and get it sent
     * @return the metric event name to be applied to the [Metric]
     */
    open fun getMetricName(): String? {
        return null
    }

    /**
     * Sends the corresponding [Metric] for the [BaseTask]
     */
    private fun sendMetric() {
        if (getMetricName().isNullOrEmpty()) return
        getSDKMetrics().sendMetric(getMetric())
    }

    /**
     * @return the final built [Metric] that should be sent for the task
     */
    private fun getMetric(): Metric {
        return Metric(getMetricName(), duration, getMetricTagsForState())
    }

    /**
     * @return a map of tags to attach to initialization tasks
     */
    private fun getMetricTagsForState(): Map<String, String> {
        return InitializeEventsMetricSender.getInstance().retryTags
    }

    /**
     * @return the component responsible for sending metrics
     */
    private fun getSDKMetrics(): ISDKMetrics = SDKMetrics.getInstance()

    /**
     * @param task is the initialization task from which we want to extract the metric name
     * @return a standardized naming format for initialization state tasks
     */
    fun getMetricNameForTask(task: Any): String? {
        val nativePrefix = "native_"
        val statePostfix = "_state"
        var className = task.javaClass.simpleName
        if (className.isEmpty()) return null
        className = className.substring(getStatePrefixLength())
            .toLowerCase() // remove InitializeState prefix
        return StringBuilder(nativePrefix.length + className.length + statePostfix.length)
            .append(nativePrefix)
            .append(className)
            .append(statePostfix)
            .toString()
    }

    /**
     * @param task is the initialization task from which we want to extract the metric name
     * @return a standardized naming format for the new initialization state tasks
     */
    fun getMetricNameForInitializeTask(name: String): String {
        return "native_${name}_task_${taskStatus}_time"
    }

    /**
     * @return length of standardized initialization state/task naming concention
     */
    private fun getStatePrefixLength(): Int {
        val initStatePrefix = "InitializeState"
        return initStatePrefix.length
    }

}