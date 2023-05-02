package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.properties.SdkProperties
import kotlinx.coroutines.withContext

class InitializeStateConfig(
    private val dispatchers: ISDKDispatchers,
    private val initializeStateConfigWithLoader: InitializeStateConfigWithLoader
) : MetricTask<InitializeStateConfig.Params, Configuration>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("config_fetch")
    }

    override suspend fun doWork(params: Params): Configuration =
        withContext(dispatchers.default) {
            DeviceLog.info("Unity Ads init: load configuration from " + SdkProperties.getConfigUrl())
            val configuration = Configuration(
                SdkProperties.getConfigUrl(),
                params.config.experimentsReader
            )

            initializeStateConfigWithLoader(InitializeStateConfigWithLoader.Params(configuration))
        }

    data class Params(val config: Configuration) : BaseParams
}