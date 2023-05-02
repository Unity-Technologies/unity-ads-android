package com.unity3d.services.core.network.mapper

import com.unity3d.services.core.network.model.BodyType
import com.unity3d.services.core.network.model.HttpRequest
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

private fun generateOkHttpBody(body: Any?): RequestBody = when(body) {
    is ByteArray -> RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), body)
    is String -> RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), body)
    else -> RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), "")
}

private fun HttpRequest.generateOkHttpHeaders(): Headers = Headers.Builder()
    .also { headers.forEach { (key, value) -> it.add(key, value.joinToString(",")) } }
    .build()

fun HttpRequest.toOkHttpRequest(): Request = Request.Builder()
    .url("${baseURL.trim('/')}/${path.trim('/')}".removeSuffix("/"))
    .method(method.toString(), body?.let(::generateOkHttpBody))
    .headers(generateOkHttpHeaders())
    .build()
