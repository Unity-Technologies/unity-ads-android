package com.unity3d.services.core.properties

import com.unity3d.services.identifiers.SessionId

interface Session {
    val id: String

    companion object Default : Session {
        override val id: String = SessionId.id
    }
}