package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.domain.getInitializationExceptionOrThrow
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.lifecycle.CachedLifecycle
import kotlinx.coroutines.withContext

/**
 * Initialization flow of the SDK
 */
class InitializeSDK(
    private val dispatchers: ISDKDispatchers,
    private val initializeStateLoadConfigFile: InitializeStateLoadConfigFile,
    private val initializeStateReset: InitializeStateReset,
    private val initializeStateError: InitializeStateError,
    private val initializeStateInitModules: InitializeStateInitModules,
    private val initializeStateConfig: InitializeStateConfig,
    private val initializeStateCreate: InitializeStateCreate,
    private val initializeStateLoadCache: InitializeStateLoadCache,
    private val initializeStateCreateWithRemote: InitializeStateCreateWithRemote,
    private val initializeStateLoadWeb: InitializeStateLoadWeb,
    private val initializeStateComplete: InitializeStateComplete
) : MetricTask<EmptyParams, Result<Unit>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("initialize")
    }

    override suspend fun doWork(params: EmptyParams): Result<Unit> =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {

                InitializeEventsMetricSender.getInstance().didInitStart()
                CachedLifecycle.register()

                // check if we have a configuration cached
                val configuration: Configuration =
                    initializeStateLoadConfigFile(InitializeStateLoadConfigFile.Params(Configuration()))
                        .getOrDefault(Configuration())

                // reset modules
                val resetResult = initializeStateReset(InitializeStateReset.Params(configuration))
                if (resetResult.isFailure) {
                    executeErrorState(ErrorState.ResetWebApp, resetResult.exceptionOrNull(), configuration)
                    throw resetResult.exceptionOrNull() ?: Exception(ErrorState.ResetWebApp.toString())
                }

                // initialize modules
                val initModulesResult = initializeStateInitModules(InitializeStateInitModules.Params(resetResult.getOrDefault(configuration)))
                if (initModulesResult.isFailure) {
                    executeErrorState(ErrorState.InitModules, resetResult.exceptionOrNull(), configuration)
                    throw initModulesResult.exceptionOrNull() ?: Exception(ErrorState.InitModules.toString())
                }

                // get native config
                val configResult = initializeStateConfig(InitializeStateConfig.Params(configuration))
                if (configResult.isFailure) {
                    handleInitializationException(configResult.getInitializationExceptionOrThrow())
                }

                if (configuration.experiments.isNativeWebViewCacheEnabled) {
                    // load webview from remote url
                    val createWithRemoteResult = initializeStateCreateWithRemote(
                        InitializeStateCreateWithRemote.Params(configResult.getOrThrow())
                    )
                    if (createWithRemoteResult.isSuccess) {
                        initializeStateComplete(InitializeStateComplete.Params(configResult.getOrThrow())).getOrThrow()
                        return@runReturnSuspendCatching
                    } else {
                        handleInitializationException(createWithRemoteResult.getInitializationExceptionOrThrow())
                    }
                }

                // load webview from cache if available and correct, if not fetch and load
                val loadCacheResult = initializeStateLoadCache(InitializeStateLoadCache.Params(configResult.getOrThrow()))
                if (loadCacheResult.isFailure) {
                    executeErrorState(ErrorState.LoadCache, loadCacheResult.exceptionOrNull(), configuration)
                    throw loadCacheResult.exceptionOrNull() ?: Exception(ErrorState.LoadCache.toString())
                }

                val loadCacheResultData = loadCacheResult.getOrNull()
                val loadWebResultData: InitializeStateLoadWeb.LoadWebResult? = if (loadCacheResultData == null) {
                    // cached data is invalid, fetch
                    val loadWebResult = initializeStateLoadWeb(InitializeStateLoadWeb.Params(configResult.getOrThrow()))
                    if (loadWebResult.isFailure) {
                        handleInitializationException(loadWebResult.getInitializationExceptionOrThrow())
                    }

                    loadWebResult.getOrThrow()
                } else {
                    null
                }

                val webviewData: String = loadWebResultData?.webViewDataString ?: loadCacheResultData ?: throw Error() // <-- Do something better@
                val webviewConfig: Configuration = loadWebResultData?.config ?: configResult.getOrThrow()

                val createResult = initializeStateCreate(InitializeStateCreate.Params(webviewConfig, webviewData))
                if (createResult.isFailure) {
                    handleInitializationException(createResult.getInitializationExceptionOrThrow())
                }

                initializeStateComplete(InitializeStateComplete.Params(configResult.getOrThrow())).getOrThrow()
            }
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