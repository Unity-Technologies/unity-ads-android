package com.unity3d.services.core.properties

import org.junit.Assert
import org.junit.Test

class SessionIdReaderTest {

    @Test
    fun getId_normalDefaultCall_receiveUuid() {
        // given
        // when
        val sessionId = SessionIdReader.sessionId

        // then
        Assert.assertEquals(36, sessionId.length)
    }

    @Test
    fun getId_normalCallMoreThanOnce_receiveUuidWithoutChanges() {
        // given
        // when
        val sessionIdFirstCall = SessionIdReader.sessionId
        val sessionIdSecondCall = SessionIdReader.sessionId

        // then
        Assert.assertEquals(36, sessionIdFirstCall.length)
        Assert.assertEquals(36, sessionIdSecondCall.length)
        Assert.assertEquals(sessionIdFirstCall, sessionIdSecondCall)

    }
}