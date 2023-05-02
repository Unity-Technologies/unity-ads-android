package com.unity3d.services.core.properties

import java.util.UUID

object SessionIdReader {
    val sessionId = UUID.randomUUID().toString()
}