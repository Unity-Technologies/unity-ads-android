package com.unity3d.services.core.network.mapper

import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.request.WebRequest

fun HttpRequest.toWebRequest(): WebRequest =
    WebRequest(
        baseURL,
        method.toString(),
        headers,
        connectTimeout,
        readTimeout,
    ).also {
        when (body) {
            is String -> it.setBody(body)
            is ByteArray -> it.body = body
        }
    }
