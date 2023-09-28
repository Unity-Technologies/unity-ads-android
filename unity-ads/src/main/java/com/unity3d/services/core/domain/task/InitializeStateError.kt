package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.log.DeviceLog
import kotlinx.coroutines.withContext

/**
 * Uses the list of [com.unity3d.services.core.configuration.IModuleConfiguration] with
 * the passed in [Configuration] and sets it in error state.
 *
 * @link [InitializeStateError.Params]
 */
class InitializeStateError(
    private val dispatchers: ISDKDispatchers
) : MetricTask<InitializeStateError.Params, Unit>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("error")
    }

    override suspend fun doWork(params: Params) =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {
                DeviceLog.error("Unity Ads init: halting init in " + params.errorState.metricName + ": " + params.exception.message)

                for (moduleName in params.config.moduleConfigurationList ?: emptyArray()) {
                    params.config.getModuleConfiguration(moduleName)?.initErrorState(
                        params.config,
                        params.errorState,
                        params.exception.message
                    )
                }
            }
        }

    /**
     * @param errorState the [ErrorState] that caused the error to happen
     * @param exception the [Exception] that was raised during the execution of another task in which the error happened
     * @param config is the current [Configuration] state
     */
    data class Params(val errorState: ErrorState, val exception: Exception, val config: Configuration) : BaseParams
}
