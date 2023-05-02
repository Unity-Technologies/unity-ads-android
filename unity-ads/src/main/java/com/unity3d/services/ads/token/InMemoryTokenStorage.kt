package com.unity3d.services.ads.token

import com.unity3d.services.core.configuration.ConfigurationReader
import com.unity3d.services.core.configuration.InitializeEventsMetricSender
import com.unity3d.services.core.configuration.PrivacyConfigStorage
import com.unity3d.services.core.device.reader.builder.DeviceInfoReaderBuilder
import com.unity3d.services.core.device.reader.GameSessionIdReader
import com.unity3d.services.core.di.IServiceComponent
import com.unity3d.services.core.di.inject
import com.unity3d.services.core.webview.WebViewApp
import com.unity3d.services.core.webview.WebViewEventCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

class InMemoryTokenStorage : TokenStorage, IServiceComponent {
    private val queue = ConcurrentLinkedQueue<String>()
    private val accessCounter = MutableStateFlow(-1)
    private val initToken = MutableStateFlow<String?>(null)
    private val executorService = Executors.newSingleThreadExecutor()
    private val asyncTokenStorage: AsyncTokenStorage by inject() // todo: remove this dependency when AsyncTokenStorage is rewrote to Kotlin (use flow instead of callback)

    @Throws(JSONException::class)
    override fun createTokens(tokens: JSONArray) {
        deleteTokens()
        appendTokens(tokens)
    }

    @Throws(JSONException::class)
    override fun appendTokens(tokens: JSONArray) {
        // -1 means deleteTokens was called before and queue should be considered as non initialized
        accessCounter.compareAndSet(-1, 0)

        val tokenLength = tokens.length()
        for (i in 0 until tokenLength) {
            queue.add(tokens.getString(i))
        }

        if (tokenLength > 0) {
            triggerTokenAvailable(false)
            asyncTokenStorage.onTokenAvailable()
        }
    }

    override fun deleteTokens() {
        queue.clear()
        accessCounter.update { -1 }
    }

    override val token: String?
        get() {
            if (accessCounter.value == -1) {
                return initToken.value
            }

            if (queue.isEmpty()) {
                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.QUEUE_EMPTY)
                return null
            }

            val accesses = accessCounter.getAndUpdate { it + 1 }
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_ACCESS, accesses)
            return queue.poll()
        }

    override val nativeGeneratedToken: Unit
        get() {
            val nativeTokenGenerator = NativeTokenGenerator(
                executorService,
                DeviceInfoReaderBuilder(
                    ConfigurationReader(),
                    PrivacyConfigStorage.getInstance(),
                    GameSessionIdReader.getInstance()
                ),
                null
            )
            nativeTokenGenerator.generateToken { token ->
                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_NATIVE_DATA, token)
            }
        }

    override fun setInitToken(value: String?) {
        if (value == null) return

        initToken.update { value }

        triggerTokenAvailable(true)
        asyncTokenStorage.onTokenAvailable()
    }

    private fun triggerTokenAvailable(withConfig: Boolean) {
        InitializeEventsMetricSender.getInstance().sdkTokenDidBecomeAvailableWithConfig(withConfig)
    }
}
