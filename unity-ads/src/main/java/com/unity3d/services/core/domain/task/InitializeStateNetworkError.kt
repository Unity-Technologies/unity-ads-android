package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.connectivity.ConnectivityMonitor
import com.unity3d.services.core.connectivity.IConnectivityListener
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.log.DeviceLog
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Using set number of connected event passed in by [Configuration] data, task will return success if connectivity returned.
 * Will return a failure if we time out waiting for connectivity to come back.
 * @link [InitializeStateNetworkError.Params]
 */
class InitializeStateNetworkError(
    private val dispatchers: ISDKDispatchers,
) : MetricTask<InitializeStateNetworkError.Params, Unit>(), IConnectivityListener {
    private var maximumConnectedEvents: Int = 500
    private var receivedConnectedEvents: Int = 0
    private var lastConnectedEventTimeMs: Long = 0
    private var connectedEventThreshold: Int = 10000
    private var continuation: Continuation<Unit>? = null

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("error_network")
    }

    override suspend fun doWork(params: Params) = withContext(dispatchers.default) {
        DeviceLog.error("Unity Ads init: network error, waiting for connection events")
        maximumConnectedEvents = params.config.maximumConnectedEvents
        connectedEventThreshold = params.config.connectedEventThreshold

        val success = withTimeoutOrNull(params.config.networkErrorTimeout) {
            suspendCancellableCoroutine<Unit> { cont ->
                startListening(cont)
            }
        }

        // We timed out
        if (success == null) {
            ConnectivityMonitor.removeListener(this@InitializeStateNetworkError)
            throw Exception("No connected events within the timeout!")
        }
    }

    private fun startListening(continuation: Continuation<Unit>) {
        this.continuation = continuation
        ConnectivityMonitor.addListener(this)
    }

    override fun onConnected() {
        receivedConnectedEvents++
        DeviceLog.debug("Unity Ads init got connected event")
        if (shouldHandleConnectedEvent()) {
            continuation?.resume(Unit)
            continuation = null
        }
        if (receivedConnectedEvents > maximumConnectedEvents) {
            ConnectivityMonitor.removeListener(this)
        }
        lastConnectedEventTimeMs = System.currentTimeMillis()
    }

    override fun onDisconnected() {
        DeviceLog.debug("Unity Ads init got disconnected event")
    }

    private fun shouldHandleConnectedEvent(): Boolean {
        return System.currentTimeMillis() - lastConnectedEventTimeMs >= connectedEventThreshold &&
            receivedConnectedEvents <= maximumConnectedEvents
    }

    data class Params(val config: Configuration) : BaseParams
}