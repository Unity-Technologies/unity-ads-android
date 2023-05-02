package com.unity3d.services.core.domain.task

import com.unity3d.services.ads.token.TokenStorage
import com.unity3d.services.core.configuration.*
import com.unity3d.services.core.device.reader.DeviceInfoDataFactory
import com.unity3d.services.core.di.get
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.AbortRetryException
import com.unity3d.services.core.extensions.withRetry
import com.unity3d.services.core.request.metrics.SDKMetrics
import com.unity3d.services.core.request.metrics.SDKMetricsSender
import com.unity3d.services.core.request.metrics.TSIMetric
import kotlinx.coroutines.withContext

/**
 * Task that issues the configuration request and parses the received data into a [Configuration] object.
 * Said [Configuration] is then provided as a result of the task execution. Will also try to do retries based
 * on provided [Configuration] and fallback to [InitializeStateNetworkError] if retry depletes.
 *
 * @link [InitializeStateConfigWithLoader.Params]
 * @return The [Configuration] received from backend server.
 */
class InitializeStateConfigWithLoader(
    private val dispatchers: ISDKDispatchers,
    private val initializeStateNetworkError: InitializeStateNetworkError,
    private val tokenStorage: TokenStorage,
    private val sdkMetricsSender: SDKMetricsSender
) : BaseTask<InitializeStateConfigWithLoader.Params, Configuration> {

    override suspend fun doWork(params: Params) =
        withContext(dispatchers.default) {
            val privacyConfigStorage = PrivacyConfigStorage.getInstance()
            val deviceInfoDataFactory = DeviceInfoDataFactory()

            var configurationLoader: IConfigurationLoader = ConfigurationLoader(
                ConfigurationRequestFactory(
                    params.config,
                    deviceInfoDataFactory.getDeviceInfoData(InitRequestType.TOKEN)
                ), get()
            )
            configurationLoader = PrivacyConfigurationLoader(
                configurationLoader,
                ConfigurationRequestFactory(
                    params.config,
                    deviceInfoDataFactory.getDeviceInfoData(InitRequestType.PRIVACY)
                ),
                privacyConfigStorage
            )
            var config = Configuration()

            val configResult = runCatching {
                withRetry(
                    retries = params.config.maxRetries,
                    scalingFactor = params.config.retryScalingFactor,
                    retryDelay = params.config.retryDelay,
                    fallbackException = InitializationException(
                        ErrorState.NetworkConfigRequest,
                        Exception(),
                        params.config
                    ),
                ) {
                    if (it > 0) InitializeEventsMetricSender.getInstance().onRetryConfig()
                    withContext(dispatchers.io) {
                        configurationLoader.loadConfiguration(object :
                            IConfigurationLoaderListener {
                            override fun onSuccess(configuration: Configuration) {
                                config = configuration
                                config.saveToDisk()
                                tokenStorage.setInitToken(config.unifiedAuctionToken)
                            }

                            override fun onError(errorMsg: String) {
                                sdkMetricsSender.sendMetric(TSIMetric.newEmergencySwitchOff())
                                // Shall return with error and stop retries
                                throw AbortRetryException(errorMsg)
                            }
                        })
                    }
                }
            }

            config = if (configResult.isFailure) {
                // Check if it as an aborted exception
                val configResultException = configResult.exceptionOrNull()
                if (configResultException is AbortRetryException) {
                    // Abort init!
                    throw InitializationException(
                        ErrorState.NetworkConfigRequest,
                        configResultException,
                        params.config
                    )
                }
                val haveNetwork =
                    runCatching { initializeStateNetworkError(InitializeStateNetworkError.Params(params.config)) }
                if (haveNetwork.isSuccess) {
                    InitializeEventsMetricSender.getInstance().onRetryConfig()
                    withContext(dispatchers.io) {
                        configurationLoader.loadConfiguration(object : IConfigurationLoaderListener {
                            override fun onSuccess(configuration: Configuration) {
                                config = configuration
                                config.saveToDisk()
                                tokenStorage.setInitToken(config.unifiedAuctionToken)
                            }

                            override fun onError(errorMsg: String) {
                                sdkMetricsSender.sendMetric(TSIMetric.newEmergencySwitchOff())
                                // Shall return with an error
                                throw InitializationException(
                                    ErrorState.NetworkConfigRequest,
                                    Exception(errorMsg),
                                    params.config
                                )
                            }
                        })
                    }
                    config
                } else {
                    throw InitializationException(
                        ErrorState.NetworkConfigRequest,
                        Exception("No connected events within the timeout!"),
                        params.config
                    )
                }
            } else {
                config
            }

            config
        }

    /**
     * @param config [Configuration] containing information to do the config request.
     */
    data class Params(val config: Configuration) : BaseParams

}
