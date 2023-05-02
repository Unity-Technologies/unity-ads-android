package com.unity3d.services.core.webview

import com.unity3d.services.core.webview.bridge.IInvocationCallbackInvoker
import com.unity3d.services.core.webview.bridge.IWebViewBridge
import com.unity3d.services.core.webview.bridge.WebViewCallback
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class InvocationTest {
    @MockK
    private lateinit var invocationCallbackInvoker: IInvocationCallbackInvoker
    @MockK
    private lateinit var webViewEventBridge: IWebViewBridge

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun sendInvocationCallback_onCreatedInvocation_callsInvokeCallback() {
        // given
        val invocation = com.unity3d.services.core.webview.bridge.Invocation(invocationCallbackInvoker, webViewEventBridge)

        // when
        invocation.sendInvocationCallback()

        // then
        verify(exactly = 1) { invocationCallbackInvoker.invokeCallback(invocation) }
    }

    @Test
    fun nextInvocation_onAddedInvocation_callsHandleInvocation() {
        // given
        val invocation = com.unity3d.services.core.webview.bridge.Invocation(invocationCallbackInvoker, webViewEventBridge)

        val className = "TestClass"
        val methodName = "TestMethod"
        val parameters = arrayOf<Any?>("test", 25)
        val webViewCallback = WebViewCallback("callback-id", invocation.id)

        invocation.addInvocation(className, methodName, parameters, webViewCallback)

        // when
        invocation.nextInvocation()

        // then
        verify(exactly = 1) { webViewEventBridge.handleInvocation(className, methodName, parameters, webViewCallback) }
    }
}
