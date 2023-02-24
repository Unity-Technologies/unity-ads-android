package com.unity3d.services.core.webview.bridge

import com.unity3d.services.core.webview.WebViewApp

object SharedInstances {
    val webViewAppInvocationCallbackInvoker : IInvocationCallbackInvoker =
        IInvocationCallbackInvoker {
                WebViewApp.getCurrentApp().invokeCallback(it)
        }
    val webViewAppNativeCallbackSubject: INativeCallbackSubject = INativeCallbackSubject {
        WebViewApp.getCurrentApp().removeCallback(it)
    }
    val webViewEventSender: IEventSender = IEventSender { eventCategory, eventId, params ->
        WebViewApp.getCurrentApp().sendEvent(eventCategory, eventId, *params)
    }
}
