package com.unity3d.services.core.webview

import com.unity3d.services.core.webview.bridge.IInvocationCallbackInvoker
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class InvocationTest {
    @MockK
    private lateinit var invocationCallbackInvoker: IInvocationCallbackInvoker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun sendInvocationCallback_onCreatedInvocation_callsInvokeCallback() {
        // given
        val invocation = com.unity3d.services.core.webview.bridge.Invocation(invocationCallbackInvoker)

        // when
        invocation.sendInvocationCallback()

        // then
        verify(exactly = 1) { invocationCallbackInvoker.invokeCallback(invocation) }
    }
}
