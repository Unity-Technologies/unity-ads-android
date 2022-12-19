package com.unity3d.services

import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.request.metrics.Metric
import com.unity3d.services.core.request.metrics.SDKMetrics
import kotlinx.coroutines.CoroutineExceptionHandler
import java.lang.IllegalStateException
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

class SDKErrorHandler(private val dispatchers: ISDKDispatchers) : CoroutineExceptionHandler {

    override val key = CoroutineExceptionHandler.Key

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val className: String = exception.stackTrace[0].fileName
        val line: Int = exception.stackTrace[0].lineNumber

        val name: String = when (exception) {
            is NullPointerException -> "native_exception_npe"
            is OutOfMemoryError -> "native_exception_oom"
            is IllegalStateException -> "native_exception_ise"
            is RuntimeException -> "native_exception_re"
            is SecurityException -> "native_exception_se"
            else -> "native_exception"
        }

        sendMetric(Metric(name, "{$className}_$line", null))
    }


    private fun sendMetric(metric: Metric) = SDKMetrics.getInstance().sendMetric(metric);
}