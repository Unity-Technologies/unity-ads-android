package com.unity3d.services.core.webview

import com.unity3d.services.core.request.metrics.Metric

fun webMessageListenerSupportedMetric() = Metric("web_message_listener_supported")
fun webMessageListenerUnsupportedMetric() = Metric("web_message_listener_unsupported")
fun webMessageListenerDisabledMetric() = Metric("web_message_listener_disabled")
fun webMessageListenerEnabledMetric() = Metric("web_message_listener_enabled")