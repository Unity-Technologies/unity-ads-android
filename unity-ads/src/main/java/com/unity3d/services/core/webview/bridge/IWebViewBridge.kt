package com.unity3d.services.core.webview.bridge

interface IWebViewBridge {
    @Throws(Exception::class)
    fun handleInvocation(
        className: String?,
        methodName: String?,
        parameters: Array<Any?>?,
        callback: WebViewCallback?
    )

    @Throws(Exception::class)
    fun handleCallback(callbackId: String?, callbackStatus: String?, parameters: Array<Any?>?)
}