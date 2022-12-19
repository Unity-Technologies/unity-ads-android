package com.unity3d.services.core.domain.task

import com.unity3d.services.core.api.Lifecycle
import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.properties.SdkProperties
import com.unity3d.services.core.webview.WebViewApp
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Uses the list of [com.unity3d.services.core.configuration.IModuleConfiguration] with
 * the passed in [Configuration] and resets its state.
 *
 * Also this tasks resets the webView app and destroys its internal WebView component.
 *
 * @link [InitializeStateReset.Params]
 */
open class InitializeStateReset(
    private val dispatchers: ISDKDispatchers,
): MetricTask<InitializeStateReset.Params, Result<Configuration>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("reset")
    }

    override suspend fun doWork(params: Params): Result<Configuration> =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {
                DeviceLog.debug("Unity Ads init: starting init")

                val currentApp : WebViewApp? = WebViewApp.getCurrentApp()

                currentApp?.resetWebViewAppInitialization()
                if (currentApp?.webView != null) {
                    val success = withTimeoutOrNull(params.config.webViewAppCreateTimeout) {
                        withContext(dispatchers.main) {
                            currentApp.webView.destroy()
                            currentApp.webView = null
                        }
                    }
                    if (success == null) {
                        throw Exception("Reset failed on opening ConditionVariable")
                    }

                }

                unregisterLifecycleCallbacks()

                SdkProperties.setCacheDirectory(null)
                SdkProperties.getCacheDirectory()?: throw Exception("Cache directory is NULL")

                SdkProperties.setInitialized(false)

                for (moduleName in params.config.moduleConfigurationList ?: emptyArray()) {
                    params.config.getModuleConfiguration(moduleName)?.resetState(params.config)
                }
                params.config
            }
        }

    private fun unregisterLifecycleCallbacks() {
        if (Lifecycle.getLifecycleListener() != null) {
            ClientProperties.getApplication()?.unregisterActivityLifecycleCallbacks(Lifecycle.getLifecycleListener())
            Lifecycle.setLifecycleListener(null)
        }
    }

    /**
     * @param config is the current [Configuration] state
     */
    data class Params(val config: Configuration) : BaseParams
}