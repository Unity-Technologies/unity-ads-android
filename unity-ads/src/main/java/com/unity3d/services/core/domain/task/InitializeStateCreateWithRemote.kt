package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.webview.WebViewApp
import kotlinx.coroutines.withContext

/**
 * Creates the [WebViewApp] with the remote url [Configuration] passed
 * in via [InitializeStateCreateWithRemote.Params]
 *
 * @link [InitializeStateCreateWithRemote.Params]
 * @return the same [Configuration] passed in via [InitializeStateCreateWithRemote.Params]
 */
class InitializeStateCreateWithRemote(
    private val dispatchers: ISDKDispatchers,
) : MetricTask<InitializeStateCreateWithRemote.Params, Result<Configuration>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("create_web_view")
    }

    override suspend fun doWork(params: Params): Result<Configuration> =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {
                DeviceLog.debug("Unity Ads init: creating webapp")

                val configuration: Configuration = params.config

                val createErrorState: ErrorState? = try {
                    WebViewApp.create(configuration, true)
                } catch (e: IllegalThreadStateException) {
                    DeviceLog.exception("Illegal Thread", e)
                    throw InitializationException(ErrorState.CreateWebApp, e, configuration)
                }

                if (createErrorState == null) {
                    configuration
                } else {
                    var errorMessage: String? =  "Unity Ads WebApp creation failed"
                    if (WebViewApp.getCurrentApp().webAppFailureMessage != null) {
                        errorMessage = WebViewApp.getCurrentApp().webAppFailureMessage
                    }
                    DeviceLog.error(errorMessage)
                    throw InitializationException(createErrorState, Exception(errorMessage), configuration)
                }
            }
        }

    data class Params(var config: Configuration) : BaseParams
}