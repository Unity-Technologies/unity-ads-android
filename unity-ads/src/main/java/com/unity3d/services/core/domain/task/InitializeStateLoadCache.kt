package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
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
 * In case of success will return [LoadCacheResult] with the full WebView data as a String and information if there is hash mismatch with [Configuration] provided.
 *
 * @link [InitializeStateLoadCache.Params]
 */
class InitializeStateLoadCache(
    private val dispatchers: ISDKDispatchers,
) : MetricTask<InitializeStateLoadCache.Params, InitializeStateLoadCache.LoadCacheResult>() {

    override fun getMetricName(): String {
        return getMetricNameForInitializeTask("read_local_webview")
    }

    override suspend fun doWork(params: Params): LoadCacheResult =
        withContext(dispatchers.default) {
            DeviceLog.debug("Unity Ads init: check if webapp can be loaded from local cache")

            val localWebViewData: ByteArray = getWebViewData() ?: return@withContext LoadCacheResult(hasHashMismatch = true)
            val localWebViewHash: String? = Utilities.Sha256(localWebViewData)

            // If charset isn't supported on device, might throw an exception (result.isFailure)
            val webViewDataString = String(localWebViewData, Charset.forName("UTF-8"))

            val hashMismatch = localWebViewHash == null || localWebViewHash != params.config.webViewHash

            if (!hashMismatch) {
                DeviceLog.info("Unity Ads init: webapp loaded from local cache")
            }

            LoadCacheResult(hashMismatch, webViewDataString)
        }

    /**
     * @param config is the current [Configuration] state
     */
    data class Params(val config: Configuration) : BaseParams

    /**
     * @param webViewData content of the WebView HTML data from cache
     * @param hasHashMismatch true if the hash from provided config differs from the cached hash
     */
    data class LoadCacheResult(val hasHashMismatch: Boolean, val webViewData: String? = null)

    private fun getWebViewData(): ByteArray? =
        try {
            Utilities.readFileBytes(File(SdkProperties.getLocalWebViewFile()))
        } catch (e: Exception) {
            DeviceLog.debug("Unity Ads init: webapp not found in local cache: " + e.message)
            null
        }
}