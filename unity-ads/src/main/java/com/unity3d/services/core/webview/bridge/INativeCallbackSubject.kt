package com.unity3d.services.core.webview.bridge

interface INativeCallbackSubject {
    fun remove(callback: NativeCallback)
    fun getCallback(callbackId: String): NativeCallback
}
