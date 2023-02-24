package com.unity3d.services.core.properties

import org.junit.Assert
import org.junit.Test

class SessionTest {

    @Test
    fun getId_normalDefaultCall_receiveUuid() {
        // given
        // when
        val sessionId = Session.Default.id

        // then
        Assert.assertEquals(36, sessionId.length)
    }
}