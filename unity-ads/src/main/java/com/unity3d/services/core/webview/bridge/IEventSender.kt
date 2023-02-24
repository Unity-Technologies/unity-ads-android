package com.unity3d.services.core.webview.bridge

fun interface IEventSender {
    fun sendEvent(eventCategory: Enum<*>, eventId: Enum<*>, vararg params: Any): Boolean
}