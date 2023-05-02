package com.unity3d.services.core.webview.bridge

import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import com.unity3d.services.core.log.DeviceLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.json.JSONArray
import org.json.JSONObject

class WebViewBridgeInterface(
    private val webViewBridge: IWebViewBridge = SharedInstances.webViewBridge,
    private val webViewAppInvocationCallbackInvoker: IInvocationCallbackInvoker = SharedInstances.webViewAppInvocationCallbackInvoker,
) {
    private fun JSONArray.toTypedArray() = (0 until length()).map(::get).toTypedArray()

    @JavascriptInterface
    fun handleInvocation(data: String) {
        DeviceLog.debug("handleInvocation $data")
        val invocationArray = JSONArray(data)
        val batch = Invocation(webViewAppInvocationCallbackInvoker, webViewBridge)

        for (idx in 0 until invocationArray.length()) {
            val currentInvocation = invocationArray[idx] as JSONArray
            val className = currentInvocation[0] as String
            val methodName = currentInvocation[1] as String
            val parameters = currentInvocation[2] as JSONArray
            val callback = currentInvocation[3] as String

            batch.addInvocation(className, methodName, parameters.toTypedArray(), WebViewCallback(callback, batch.id))
            batch.nextInvocation()
        }

        batch.sendInvocationCallback()
    }

    @JavascriptInterface
    fun handleCallback(callbackId: String, callbackStatus: String, rawParameters: String) {
        DeviceLog.debug("handleCallback $callbackId $callbackStatus $rawParameters")
        val parameters = JSONArray(rawParameters)

        webViewBridge.handleCallback(callbackId, callbackStatus, parameters.toTypedArray())
    }

    fun onHandleInvocation(view: WebView, message: WebMessageCompat, sourceOrigin: Uri, isMainFrame: Boolean, replyProxy: JavaScriptReplyProxy) {
        val data = message.data

        if (!isMainFrame || data.isNullOrBlank()) return

        handleInvocation(data)
    }

    fun onHandleCallback(view: WebView, message: WebMessageCompat, sourceOrigin: Uri, isMainFrame: Boolean, replyProxy: JavaScriptReplyProxy) {
        val data = message.data

        if (!isMainFrame || data.isNullOrBlank()) return

        val parameters = JSONObject(data)

        val callbackId = parameters.getString("id")
        val callbackStatus = parameters.getString("status")
        val rawParameters = parameters.getString("parameters")

        handleCallback(callbackId, callbackStatus, rawParameters)
    }
}
