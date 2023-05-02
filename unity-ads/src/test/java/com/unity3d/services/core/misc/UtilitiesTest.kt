package com.unity3d.services.core.misc

import com.unity3d.services.core.log.DeviceLog
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class UtilitiesTest {
    @MockK
    private lateinit var runnableMock: Runnable

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun wrapCustomerListener_runnableThrows_errorDeviceLog() {
        mockkStatic(DeviceLog::class) {
            // given
            every { runnableMock.run() } throws Exception("error")

            // when
            Utilities.wrapCustomerListener(runnableMock)

            // then
            verify (exactly = 1) { runnableMock.run() }
            verify (exactly = 1) { DeviceLog.error("An uncaught exception has occurred in the client application.  Exception: error") }
        }
    }

    @Test
    fun wrapCustomerListener_runnableOk_continueRunning() {
        mockkStatic(DeviceLog::class) {
            // given
            // when
            Utilities.wrapCustomerListener(runnableMock)

            // then
            verify (exactly = 0) { DeviceLog.error(any()) }
            verify (exactly = 1) { runnableMock.run() }
        }
    }
}