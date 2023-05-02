package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.domain.getInitializationExceptionOrThrow
import com.unity3d.services.core.lifecycle.CachedLifecycle
import com.unity3d.services.core.log.DeviceLog
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Initialization flow of the SDK
 */
class InitializeSDK(
    private val dispatchers: ISDKDispatchers,
    private val configFileFromLocalStorage: ConfigFileFromLocalStorage,
    private val initializeStateReset: InitializeStateReset,
    private val initializeStateError: InitializeStateError,
    private val initializeStateConfig: InitializeStateConfig,
    private val initializeStateCreate: InitializeStateCreate,
    private val initializeStateLoadCache: InitializeStateLoadCache,
    private val initializeStateCreateWithRemote: InitializeStateCreateWithRemote,
    private val initializeStateLoadWeb: InitializeStateLoadWeb,
    private val initializeStateComplete: InitializeStateComplete
) : MetricTask<EmptyParams, Unit>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("initialize")
    }

    override suspend fun doWork(params: EmptyParams) =
        withContext(dispatchers.default) {
            InitializeEventsMetricSender.getInstance().didInitStart()
            CachedLifecycle.register()

            // check if we have a configuration cached
            DeviceLog.debug("Unity Ads Init: Loading Config File From Local Storage")
            val configuration = runCatching { configFileFromLocalStorage(ConfigFileFromLocalStorage.Params()) }
                .onFailure { DeviceLog.debug("Unity Ads Init: Could not load config file from local storage: ${it.message}") }
                .getOrDefault(Configuration())

            // reset modules
            val resetResult = runCatching { initializeStateReset(InitializeStateReset.Params(configuration)) }
            if (resetResult.isFailure) {
                executeErrorState(ErrorState.ResetWebApp, resetResult.exceptionOrNull(), configuration)
                throw resetResult.exceptionOrNull() ?: Exception(ErrorState.ResetWebApp.toString())
            }

            // get native config
            val configResult = runCatching { initializeStateConfig(InitializeStateConfig.Params(configuration)) }
            if (configResult.isFailure) {
                handleInitializationException(configResult.getInitializationExceptionOrThrow())
            }

            if (configuration.experiments.isNativeWebViewCacheEnabled) {
                // load webview from remote url
                val createWithRemoteResult = runCatching {
                    initializeStateCreateWithRemote(
                        InitializeStateCreateWithRemote.Params(configResult.getOrThrow())
                    )
                }
                if (createWithRemoteResult.isSuccess) {
                    initializeStateComplete(InitializeStateComplete.Params(configResult.getOrThrow()))
                    return@withContext
                } else {
                    handleInitializationException(createWithRemoteResult.getInitializationExceptionOrThrow())
                }
            }

            // load WebView from cache if available and correct, if not fetch and load
            val loadCacheResult =
                runCatching { initializeStateLoadCache(InitializeStateLoadCache.Params(configResult.getOrThrow())) }
            if (loadCacheResult.isFailure) {
                executeErrorState(ErrorState.LoadCache, loadCacheResult.exceptionOrNull(), configuration)
                throw loadCacheResult.exceptionOrNull() ?: Exception(ErrorState.LoadCache.toString())
            }

            val loadCacheResultData = loadCacheResult.getOrThrow()
            val webViewData: String = if (loadCacheResultData.hasHashMismatch) {
                // cached data has mismatch on checksum
                if (configuration.experiments.isWebViewAsyncDownloadEnabled && loadCacheResultData.webViewData != null) {
                    // Fire and forget webView download, will be used in next session.
                    launch(CoroutineName("LaunchLoadWeb")) { runCatching { initializeStateLoadWeb(InitializeStateLoadWeb.Params(configResult.getOrThrow())) }}
                    loadCacheResultData.webViewData
                } else {
                    // We don't have any cached WebView, we must wait for download to complete to continue init
                    val loadWebResult = runCatching { initializeStateLoadWeb(InitializeStateLoadWeb.Params(configResult.getOrThrow())) }
                    if (loadWebResult.isFailure) {
                        handleInitializationException(loadWebResult.getInitializationExceptionOrThrow())
                    }
                    loadWebResult.getOrThrow().webViewDataString
                }
            } else {
                checkNotNull(loadCacheResultData.webViewData) { "WebView is missing." }
            }

            val createResult =
                runCatching { initializeStateCreate(InitializeStateCreate.Params(configResult.getOrThrow(), webViewData)) }
            if (createResult.isFailure) {
                handleInitializationException(createResult.getInitializationExceptionOrThrow())
            }

            initializeStateComplete(InitializeStateComplete.Params(configResult.getOrThrow()))
            return@withContext
        }

    private suspend fun handleInitializationException(exception: InitializationException) {
        executeErrorState(exception.errorState, exception.originalException, exception.config)
        throw exception
    }

    private suspend fun executeErrorState(
        errorState: ErrorState,
        taskException: Throwable?,
        configuration: Configuration
    ) =
        initializeStateError(
            InitializeStateError.Params(
                errorState = errorState,
                exception = Exception(taskException?.message),
                config = configuration
            )
        )

}
