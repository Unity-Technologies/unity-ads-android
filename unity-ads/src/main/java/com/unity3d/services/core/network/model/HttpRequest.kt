package com.unity3d.services.core.network.model

/**
 * Data class representing a generic http request
 */

data class HttpRequest @JvmOverloads constructor(
    val baseURL: String,
    val path: String = "",
    val method: RequestType = RequestType.GET,
    val body: Any? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val parameters: Map<String, String> = emptyMap(),
    val bodyType: BodyType = BodyType.UNKNOWN,
    val scheme: String = DEFAULT_SCHEME,
    val port: Int? = null,
    val connectTimeout: Int = DEFAULT_TIMEOUT,
    val readTimeout: Int = DEFAULT_TIMEOUT,
    val writeTimeout: Int = DEFAULT_TIMEOUT,
    val callTimeout: Int = DEFAULT_TIMEOUT,
) {
    companion object {
        private const val DEFAULT_TIMEOUT: Int = 30_000 // seconds
        private const val DEFAULT_SCHEME: String = "https"
    }
}
