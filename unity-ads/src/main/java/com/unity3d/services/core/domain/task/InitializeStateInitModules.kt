package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import kotlinx.coroutines.withContext

/**
 * Initializes the list of [com.unity3d.services.core.configuration.IModuleConfiguration] with
 * the passed in [Configuration]
 *
 * @link [InitializeStateInitModules.Params]
 * @return the same [Configuration] passed in via [InitializeStateInitModules.Params]
 */
class InitializeStateInitModules(
    private val dispatchers: ISDKDispatchers,
): MetricTask<InitializeStateInitModules.Params, Result<Configuration>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("modules_initialization")
    }

    override suspend fun doWork(params: Params): Result<Configuration> =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {
                for (moduleName in params.config.moduleConfigurationList ?: emptyArray()) {
                    if (params.config.getModuleConfiguration(moduleName)?.initModuleState(params.config) == false) {
                        throw Exception("Unity Ads config server resolves to loopback address (due to ad blocker?)")
                    }
                }
                params.config
            }
        }

    /**
     * @param config is the current [Configuration] state
     */
    data class Params(val config: Configuration) : BaseParams
}