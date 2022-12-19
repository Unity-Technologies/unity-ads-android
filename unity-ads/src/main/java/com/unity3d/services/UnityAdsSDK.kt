package com.unity3d.services

import com.unity3d.services.core.di.IServiceComponent
import com.unity3d.services.core.di.ServiceProvider.NAMED_SDK
import com.unity3d.services.core.di.inject
import com.unity3d.services.core.domain.task.EmptyParams
import com.unity3d.services.core.domain.task.InitializeSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * SDK Kotlin entry point
 */
object UnityAdsSDK : IServiceComponent {

    private val sdkScope: CoroutineScope by inject(NAMED_SDK)
    private val initializeSDK: InitializeSDK by inject()

    /**
     * Initialize the SDK
     */
    fun initialize() = sdkScope.launch {
        initializeSDK(EmptyParams)
    }
}