package com.unity3d.services.core.network.core

import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.HttpResponse

/**
 * Abstraction to be able to swap out different http client implementations
 */
interface HttpClient {
    @Throws(Exception::class)
    fun executeBlocking(request: HttpRequest): HttpResponse
    suspend fun execute(request: HttpRequest): HttpResponse
}