package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import kotlinx.coroutines.withContext

class InitializeStateComplete(
    private val dispatchers: ISDKDispatchers,
) : MetricTask<InitializeStateComplete.Params, Unit>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("completion")
    }

    override suspend fun doWork(params: Params): Result<Unit> = withContext(dispatchers.default) {
        runReturnSuspendCatching {
            for (moduleName in params.config.moduleConfigurationList) {
                params.config.getModuleConfiguration(moduleName)?.initCompleteState(params.config)
            }
        }
    }

    data class Params(val config: Configuration) : BaseParams
}