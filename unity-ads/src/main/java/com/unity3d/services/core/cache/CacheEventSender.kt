package com.unity3d.services.core.cache

import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import java.io.Serializable

class CacheEventSender(private val eventSender: IEventSender) : Serializable {
    fun sendEvent(eventId: CacheEvent, vararg params: Any): Boolean {
        return eventSender.sendEvent(WebViewEventCategory.CACHE, eventId, *params)
    }
}
