package com.unity3d.services.core.di

import com.unity3d.services.SDKErrorHandler
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.domain.SDKDispatchers
import com.unity3d.services.core.domain.task.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

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
        // cogs
        single { provideSDKDispatchers() }
        single(NAMED_SDK) { provideSDKErrorHandler(get()) }
        single(NAMED_SDK) { provideSDKScope(get(), get(NAMED_SDK)) }

        // tasks
        factory { InitializeStateNetworkError(get()) }
        single { InitializeStateLoadConfigFile(get()) }
        single { InitializeStateReset(get()) }
        single { InitializeStateError(get()) }
        single { InitializeStateInitModules(get()) }
        single { InitializeStateConfigWithLoader(get(), get()) }
        single { InitializeStateConfig(get(), get()) }
        single { InitializeStateCreate(get()) }
        single { InitializeStateLoadCache(get()) }
        single { InitializeStateCreateWithRemote(get()) }
        single { InitializeStateLoadWeb(get(), get()) }
        single { InitializeStateComplete(get()) }
        single { InitializeSDK(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
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
    private fun provideSDKErrorHandler(dispatchers: ISDKDispatchers): CoroutineExceptionHandler {
        return SDKErrorHandler(dispatchers)
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

}