package com.unity3d.services.core.properties

interface Session {
    val id: String

    companion object Default : Session {
        override val id: String = SessionIdReader.sessionId
    }
}