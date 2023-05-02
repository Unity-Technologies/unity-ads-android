package com.unity3d.services.core.network.core

import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.network.mapper.toWebRequest
import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.HttpResponse
import com.unity3d.services.core.request.WebRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * A native implementation of [HttpClient] without requiring a dependency based on [WebRequest]
 * Limited to http 1.1
 */
class LegacyHttpClient(
    private val dispatchers: ISDKDispatchers,
) : HttpClient {

    /**
     * Helper method that blocks the thread to be used for Java interaction
     *
     * @param request [HttpRequest] to be executes on the network
     * @return [HttpResponse] of the passed in [HttpRequest]
     */
    override fun executeBlocking(request: HttpRequest): HttpResponse = runBlocking(dispatchers.io) {
        execute(request)
    }

    /**
     * Executes an http network request
     *
     * @param request [HttpRequest] to be executes on the network
     * @return [HttpResponse] of the passed in [HttpRequest]
     */
    override suspend fun execute(request: HttpRequest): HttpResponse = withContext(dispatchers.io) {
        val webRequest = request.toWebRequest()
        val response = webRequest.makeRequest()
        HttpResponse(
            statusCode = webRequest.responseCode,
            headers = webRequest.headers,
            urlString = webRequest.url.toString(),
            body = response ?: "",
        )
    }
}
