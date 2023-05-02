package com.unity3d.services.core.webview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.unity3d.services.core.configuration.Experiments
import com.unity3d.services.core.configuration.IExperiments
import com.unity3d.services.core.di.IServiceComponent
import com.unity3d.services.core.di.inject
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.misc.Utilities
import com.unity3d.services.core.misc.ViewUtilities
import com.unity3d.services.core.request.metrics.SDKMetricsSender
import com.unity3d.services.core.webview.bridge.IInvocationCallbackInvoker
import com.unity3d.services.core.webview.bridge.IWebViewBridge
import com.unity3d.services.core.webview.bridge.SharedInstances
import com.unity3d.services.core.webview.bridge.WebViewBridgeInterface

open class WebView @JvmOverloads constructor(
    context: Context,
    shouldNotRequireGesturePlayback: Boolean = false,
    webViewBridge: IWebViewBridge = SharedInstances.webViewBridge,
    callbackInvoker: IInvocationCallbackInvoker = SharedInstances.webViewAppInvocationCallbackInvoker,
    experiments: IExperiments = Experiments(),
) : WebView(context), IServiceComponent {
    private val sdkMetricsSender by inject<SDKMetricsSender>()
    private val webViewBridgeInterface = WebViewBridgeInterface(webViewBridge, callbackInvoker)

    init {
        with(settings) {
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccess = true
            blockNetworkImage = false
            blockNetworkLoads = false
            builtInZoomControls = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            databaseEnabled = false
            displayZoomControls = false
            domStorageEnabled = false
            setEnableSmoothTransition(false)
            setGeolocationEnabled(false)
            javaScriptCanOpenWindowsAutomatically = false
            javaScriptEnabled = true
            lightTouchEnabled = false
            loadWithOverviewMode = false
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            if (Build.VERSION.SDK_INT >= 21) {
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }
            setNeedInitialFocus(true)
            pluginState = WebSettings.PluginState.OFF
            setRenderPriority(WebSettings.RenderPriority.NORMAL)
            saveFormData = false
            savePassword = false
            setSupportMultipleWindows(false)
            setSupportZoom(false)
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = !shouldNotRequireGesturePlayback
        }

        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        setInitialScale(0)
        setBackgroundColor(Color.TRANSPARENT)
        ViewUtilities.setBackground(this, ColorDrawable(Color.TRANSPARENT))
        setBackgroundResource(0)

        val isWebMessageEnabled = experiments.isWebMessageEnabled
        if (isWebMessageEnabled) {
            sdkMetricsSender.sendMetric(webMessageListenerEnabledMetric())
        } else {
            sdkMetricsSender.sendMetric(webMessageListenerDisabledMetric())
        }

        val isWebMessageListenerSupported = WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)
        if (isWebMessageListenerSupported) {
            sdkMetricsSender.sendMetric(webMessageListenerSupportedMetric())
        } else {
            sdkMetricsSender.sendMetric(webMessageListenerUnsupportedMetric())
        }

        if (isWebMessageEnabled && isWebMessageListenerSupported) {
            // todo: improve implementation by using set of origins
            WebViewCompat.addWebMessageListener(this, "handleInvocation", setOf("*"), webViewBridgeInterface::onHandleInvocation)
            WebViewCompat.addWebMessageListener(this, "handleCallback", setOf("*"), webViewBridgeInterface::onHandleCallback)
        } else {
            addJavascriptInterface(webViewBridgeInterface, "webviewbridge")
        }
    }

    override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
        Utilities.runOnUiThread {
            super.evaluateJavascript(script, resultCallback)
        }
    }

    override fun loadUrl(url: String) {
        DeviceLog.debug("Loading url: $url")
        super.loadUrl(url)
    }
}
