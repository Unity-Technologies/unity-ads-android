package com.unity3d.services.core.di

import android.os.Handler
import android.os.Looper
import com.unity3d.services.SDKErrorHandler
import com.unity3d.services.ads.measurements.MeasurementsService
import com.unity3d.services.ads.token.AsyncTokenStorage
import com.unity3d.services.ads.token.InMemoryAsyncTokenStorage
import com.unity3d.services.ads.token.InMemoryTokenStorage
import com.unity3d.services.ads.token.TokenStorage
import com.unity3d.services.ads.topics.TopicsService
import com.unity3d.services.core.device.VolumeChange
import com.unity3d.services.core.device.VolumeChangeContentObserver
import com.unity3d.services.core.device.VolumeChangeMonitor
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.domain.SDKDispatchers
import com.unity3d.services.core.domain.task.*
import com.unity3d.services.core.network.core.LegacyHttpClient
import com.unity3d.services.core.network.core.HttpClient
import com.unity3d.services.core.properties.ClientProperties
import com.unity3d.services.core.network.core.OkHttp3Client
import com.unity3d.services.core.request.metrics.SDKMetrics
import com.unity3d.services.core.request.metrics.SDKMetricsSender
import com.unity3d.services.core.webview.bridge.SharedInstances
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient

/**
 * Main service provider containing and instantiating required dependencies
 */
object ServiceProvider : IServiceProvider {

    const val NAMED_SDK: String = "sdk"

    private val serviceRegistry: IServicesRegistry = initialize()

    /**
     * Initialize the [ServicesRegistry] with the services the SDK requires
     */
    override fun initialize(): IServicesRegistry = registry {
        // logging and metrics
        single { provideSDKMetricSender() }

        // cogs
        single { provideSDKDispatchers() }
        single(NAMED_SDK) { provideSDKErrorHandler(get(), get()) }
        single(NAMED_SDK) { provideSDKScope(get(), get(NAMED_SDK)) }

        // network
        single { provideHttpClient(get(), get()) }

        // tasks
        factory { InitializeStateNetworkError(get()) }
        single { ConfigFileFromLocalStorage(get()) }
        single { InitializeStateReset(get()) }
        single { InitializeStateError(get()) }
        single { InitializeStateConfigWithLoader(get(), get(), get(), get()) }
        single { InitializeStateConfig(get(), get()) }
        single { InitializeStateCreate(get()) }
        single { InitializeStateLoadCache(get()) }
        single { InitializeStateCreateWithRemote(get()) }
        single { InitializeStateLoadWeb(get(), get(), get()) }
        single { InitializeStateComplete(get()) }
        single {
            InitializeSDK(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        // repositories
        single<TokenStorage> { InMemoryTokenStorage() }
        single<AsyncTokenStorage> { InMemoryAsyncTokenStorage(null, Handler(Looper.getMainLooper()), SDKMetrics.getInstance(), get()) }

        // monitoring
        single<VolumeChange> { VolumeChangeContentObserver() }
        single { VolumeChangeMonitor(SharedInstances.webViewEventSender, get()) }

        // android privacy sandbox
        single { MeasurementsService(ClientProperties.getApplicationContext(), get(), SharedInstances.webViewEventSender) }
        single { TopicsService(ClientProperties.getApplicationContext(), get(), SharedInstances.webViewEventSender) }
    }

    /**
     * @return the current [IServicesRegistry] tied to the [ServiceProvider]
     */
    override fun getRegistry(): IServicesRegistry = serviceRegistry

    /**
     * Provides an SDK wide thread handling for tasks to be executed on
     */
    private fun provideSDKDispatchers(): ISDKDispatchers {
        return SDKDispatchers()
    }

    /**
     * SDK [CoroutineExceptionHandler] for capturing any unhandled exceptions at a parent level
     */
    private fun provideSDKErrorHandler(dispatchers: ISDKDispatchers, sdkMetricsSender: SDKMetricsSender): CoroutineExceptionHandler {
        return SDKErrorHandler(dispatchers, sdkMetricsSender)
    }

    /**
     * SDK parent [CoroutineScope], won't crash on exceptions, will catch/handle all children's exceptions
     */
    private fun provideSDKScope(
        dispatchers: ISDKDispatchers,
        errorHandler: CoroutineExceptionHandler
    ): CoroutineScope {
        return CoroutineScope(dispatchers.default + SupervisorJob() + errorHandler)
    }

    private fun provideSDKMetricSender(): SDKMetricsSender {
        // TODO: Switch implementation to use injection for HttpClient, Configuration, etc.
        return SDKMetrics.getInstance()
    }

    /**
     * Provides an [HttpClient] to be used for the network needs of the SDK
     */
    private fun provideHttpClient(dispatchers: ISDKDispatchers, configFileFromLocalStorage: ConfigFileFromLocalStorage): HttpClient {
        val config = runBlocking {
            configFileFromLocalStorage(ConfigFileFromLocalStorage.Params()).getOrNull()
        }

        if (config?.experiments?.isOkHttpEnabled == true) {
            return OkHttp3Client(dispatchers, OkHttpClient())
        }

        return LegacyHttpClient(dispatchers)
    }
}
