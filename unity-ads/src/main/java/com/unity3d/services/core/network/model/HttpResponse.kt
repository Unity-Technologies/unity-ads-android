package com.unity3d.services.core.network.model

/**
 * Data class representing a generic http response
 */
data class HttpResponse @JvmOverloads constructor(
    val body: Any,
    val statusCode: Int = 200,
    val headers: Map<String, Any> = emptyMap(),
    val urlString: String = "",
)
