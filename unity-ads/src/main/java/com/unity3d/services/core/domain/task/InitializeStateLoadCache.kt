package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.properties.SdkProperties
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

/**
 * Loads WebView data from the file storage.
 * Ensures the hash is the same one from the provided [Configuration].
 * Result will contain null in case of a mismatch between the [Configuration] SHA-256 value and the file checksum.
 * In case of success will return the full WebView data as a String
 *
 * @link [InitializeStateLoadCache.Params]
 */
class InitializeStateLoadCache(
    private val dispatchers: ISDKDispatchers,
) : MetricTask<InitializeStateLoadCache.Params, Result<String?>>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("read_local_webview")
    }

    override suspend fun doWork(params: Params): Result<String?> =
        withContext(dispatchers.default) {
            runReturnSuspendCatching {
                DeviceLog.debug("Unity Ads init: check if webapp can be loaded from local cache")

                val localWebViewData: ByteArray = getWebViewData() ?: return@runReturnSuspendCatching null
                val localWebViewHash: String? = Utilities.Sha256(localWebViewData)

                if (localWebViewHash != null && localWebViewHash == params.config.webViewHash) {
                    // If charset isn't supported on device, might throw an exception (result.isFailure)
                    val webViewDataString = String(localWebViewData, Charset.forName("UTF-8"))
                    DeviceLog.info("Unity Ads init: webapp loaded from local cache")
                    return@runReturnSuspendCatching webViewDataString
                }
                null
            }
        }
    /**
    * @param config is the current [Configuration] state
    */
    data class Params(val config : Configuration) : BaseParams

    private fun getWebViewData() : ByteArray? =
        try {
            Utilities.readFileBytes(File(SdkProperties.getLocalWebViewFile()))
        } catch (e: Exception) {
            DeviceLog.debug("Unity Ads init: webapp not found in local cache: " + e.message)
            null
        }
}