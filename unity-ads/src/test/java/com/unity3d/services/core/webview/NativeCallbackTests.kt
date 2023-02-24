package com.unity3d.services.core.webview

import com.unity3d.services.core.webview.bridge.CallbackStatus
import com.unity3d.services.core.webview.bridge.INativeCallbackSubject
import com.unity3d.services.core.webview.bridge.NativeCallback
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Method
import kotlin.test.assertFailsWith


class NativeCallbackTests {
    @MockK
    lateinit var nativeCallbackSubject: INativeCallbackSubject

    @MockK
    lateinit var method: Method

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun invoke_providedAllParams_invokeCalledOnceAndReleaseCalled() {
        // given
        val callback = NativeCallback(method, nativeCallbackSubject)

        // when
        callback.invoke("OK")

        // then
        verify(exactly = 1) { method.invoke(null, CallbackStatus.OK) }
        verify(exactly = 1) { nativeCallbackSubject.remove(callback) }
    }

    @Test
    fun invoke_callbackStatusIncorrect_invokeCalledOnceAndReleaseCalledAndThrowsException() {
        // given
        val callback = NativeCallback(method, nativeCallbackSubject)

        // when
        assertFailsWith(Exception::class) {
            callback.invoke("OT-OK")
        }

        // then
        verify(exactly = 0) { method.invoke(null, CallbackStatus.OK) }
        verify(exactly = 1) { nativeCallbackSubject.remove(callback) }
    }
}