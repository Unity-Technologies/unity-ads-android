package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Loads [Configuration] from local storage and returns the loaded [Configuration] data.
 * In case where the file is missing or content is corrupted, the provided [Configuration] will be returned.
 * @link [InitializeStateLoadConfigFile.Params]
 */
class InitializeStateLoadConfigFile(
    private val dispatchers: ISDKDispatchers,
): MetricTask<InitializeStateLoadConfigFile.Params,  Result<Configuration>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("read_local_config")
    }

    override suspend fun doWork(params: Params): Result<Configuration> =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {
                DeviceLog.debug("Unity Ads init: Loading Config File Parameters")

                val configFile = File(SdkProperties.getLocalConfigurationFilepath())
                var configuration = params.config
                // Attempt to overwrite default configuration with local configuration
                try {
                    val fileContent = String(Utilities.readFileBytes(configFile))
                    val loadedJson = JSONObject(fileContent)
                    configuration = Configuration(loadedJson)
                } catch (e: Exception) {
                    DeviceLog.debug("Unity Ads init: Using default configuration parameters")
                }
                configuration
            }
        }

    /**
     * @params [Configuration] that will be returned if the config file is missing or invalid.
     */
    data class Params(val config: Configuration) : BaseParams
}