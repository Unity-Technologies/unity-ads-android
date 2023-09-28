package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.properties.SdkProperties
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Loads [Configuration] from local storage and returns the loaded [Configuration] data.
 * In case where the file is missing or content is corrupted, the provided [Configuration] will be returned.
 * @link [ConfigFileFromLocalStorage.Params]
 */
class ConfigFileFromLocalStorage(
    private val dispatchers: ISDKDispatchers,
) : MetricTask<ConfigFileFromLocalStorage.Params, Configuration>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("read_local_config")
    }

    override suspend fun doWork(params: Params): Result<Configuration> =
        withContext(dispatchers.io) {
            runReturnSuspendCatching {
                val configFile = File(SdkProperties.getLocalConfigurationFilepath())
                val fileContent = configFile.readText()
                val loadedJson = JSONObject(fileContent)
                Configuration(loadedJson)
            }
        }

    /**
     * @params [Configuration] that will be returned if the config file is missing or invalid.
     */
    class Params : BaseParams
}
