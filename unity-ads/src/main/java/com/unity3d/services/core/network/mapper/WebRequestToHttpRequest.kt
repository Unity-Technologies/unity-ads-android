package com.unity3d.services.core.network.mapper

import com.unity3d.services.core.network.model.HttpRequest
import com.unity3d.services.core.network.model.RequestType
import com.unity3d.services.core.request.WebRequest

fun WebRequest.toHttpRequest(): HttpRequest =
    HttpRequest(
        baseURL = url.toString(),
        method = RequestType.valueOf(requestType),
        headers = headers,
        body = body,
    )
