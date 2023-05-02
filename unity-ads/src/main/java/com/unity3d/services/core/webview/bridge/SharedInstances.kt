package com.unity3d.services.core.webview.bridge

import com.unity3d.services.core.webview.WebViewApp

object SharedInstances {
    val webViewAppInvocationCallbackInvoker : IInvocationCallbackInvoker =
        IInvocationCallbackInvoker {
                WebViewApp.getCurrentApp().invokeCallback(it)
        }
    val webViewAppNativeCallbackSubject: INativeCallbackSubject = object: INativeCallbackSubject {
        override fun remove(callback: NativeCallback) {
            WebViewApp.getCurrentApp().removeCallback(callback)
        }

        override fun getCallback(callbackId: String): NativeCallback {
            return WebViewApp.getCurrentApp().getCallback(callbackId)
        }

    }
    val webViewEventSender: IEventSender = object: IEventSender {
        override fun sendEvent(
            eventCategory: Enum<*>,
            eventId: Enum<*>,
            vararg params: Any
        ): Boolean {
            return WebViewApp.getCurrentApp()?.sendEvent(eventCategory, eventId, *params) ?: false
        }

        override fun canSend(): Boolean {
            return WebViewApp.getCurrentApp() != null
        }
    }
    val webViewBridge: IWebViewBridge = object : IWebViewBridge {
        @Throws(Exception::class)
        override fun handleInvocation(
            className: String?,
            methodName: String?,
            parameters: Array<Any?>?,
            callback: WebViewCallback?
        ) {
            WebViewBridge.getInstance().handleInvocation(
                className,
                methodName,
                parameters,
                callback
            )
        }

        @Throws(Exception::class)
        override fun handleCallback(
            callbackId: String?,
            callbackStatus: String?,
            parameters: Array<Any?>?
        ) {
            WebViewBridge.getInstance().handleCallback(callbackId, callbackStatus, parameters)
        }
    }
}
