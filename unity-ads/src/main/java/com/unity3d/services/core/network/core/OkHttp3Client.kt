package com.unity3d.services.core.network.core

import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.network.mapper.toOkHttpRequest
import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.HttpResponse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * An implementation of [HttpClient] based on OkHttp
 * Supports Http2
 */
class OkHttp3Client(
    private val dispatchers: ISDKDispatchers,
    private val client: OkHttpClient,
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
        val okHttpRequest = request.toOkHttpRequest()
        val response = makeRequest(okHttpRequest, request.connectTimeout.toLong(), request.readTimeout.toLong())

        HttpResponse(
            statusCode = response.code(),
            headers = response.headers().toMultimap(),
            urlString = response.request().url().toString(),
            body = response.body()?.string() ?: ""
        )
    }

    /**
     * Wraps the OkHttp call callback in a coroutine with structured concurrency
     */
    private suspend fun makeRequest(request: Request, connectTimeout: Long, readTimeout: Long): Response = suspendCancellableCoroutine { continuation ->
        val configuredClient = client.newBuilder()
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .build()

        configuredClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}
