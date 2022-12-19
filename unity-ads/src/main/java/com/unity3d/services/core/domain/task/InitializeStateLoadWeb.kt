package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.extensions.withRetry
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.request.WebRequest
import kotlinx.coroutines.withContext
import java.io.File
import java.net.MalformedURLException

/**
 * Fetches the WebView HTML using a [WebRequest] by querying the [Configuration.getWebViewUrl] passed in.
 * This task will retry using provided [Configuration] parameters and fallback to [InitializeStateNetworkError] in case of retry failure.
 *
 * Upon success, will return a [LoadWebResult] containing the string webview data alongside provided configuration
 * @link [InitializeStateLoadConfigFile.Params]
 */
class InitializeStateLoadWeb(
    private val dispatchers: ISDKDispatchers,
    private val initializeStateNetworkError: InitializeStateNetworkError
) : MetricTask<InitializeStateLoadWeb.Params, Result<InitializeStateLoadWeb.LoadWebResult>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("download_web_view")
    }

    override suspend fun doWork(params: Params): Result<LoadWebResult> = withContext(dispatchers.default) {
        runReturnSuspendCatching {
            DeviceLog.info("Unity Ads init: loading webapp from " + params.config.webViewUrl)

            val request: WebRequest = try {
                WebRequest(params.config.webViewUrl, "GET", null)
            } catch (e: MalformedURLException) {
                DeviceLog.exception("Malformed URL", e)
                throw InitializationException(ErrorState.MalformedWebviewRequest, e, params.config)
            }

            val webViewDataResult = runCatching {
                withRetry(
                    retries = params.config.maxRetries,
                    scalingFactor = params.config.retryScalingFactor,
                    retryDelay = params.config.retryDelay,
                    fallbackException = InitializationException(ErrorState.NetworkWebviewRequest, Exception(), params.config),
                ) {
                    if (it > 0) InitializeEventsMetricSender.getInstance().onRetryWebview()
                    withContext(dispatchers.io) { request.makeRequest() }
                }
            }

            val webViewData: String = if (webViewDataResult.isFailure) {
                val haveNetwork: Result<Unit> = initializeStateNetworkError(InitializeStateNetworkError.Params(params.config))
                if (haveNetwork.isSuccess) {
                    withContext(dispatchers.io) { request.makeRequest() }
                } else {
                    throw InitializationException(ErrorState.NetworkWebviewRequest, Exception("No connected events within the timeout!"), params.config)
                }
            } else {
                webViewDataResult.getOrThrow()
            }

            val webViewHash: String? = params.config.webViewHash
            if (webViewHash != null && Utilities.Sha256(webViewData) != webViewHash) {
                throw InitializationException(ErrorState.InvalidHash, Exception("Invalid webViewHash"), params.config)
            }

            if (webViewHash != null) {
                Utilities.writeFile(File(SdkProperties.getLocalWebViewFile()), webViewData)
            }

            LoadWebResult(params.config, webViewData)

        }
    }

    data class Params(val config: Configuration) : BaseParams

    /**
     * @param config [Configuration] provided to get the WebView data
     * @param webViewDataString content of the WebView HTML data
     */
    data class LoadWebResult(val config: Configuration, val webViewDataString: String)

}