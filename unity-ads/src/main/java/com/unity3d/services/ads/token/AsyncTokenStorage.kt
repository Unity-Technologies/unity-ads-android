package com.unity3d.services.ads.token

import com.unity3d.services.ads.gmascar.managers.IBiddingManager
import com.unity3d.services.core.configuration.Configuration

interface AsyncTokenStorage {
    fun setConfiguration(configuration: Configuration?)
    fun onTokenAvailable()
    fun getToken(biddingManager: IBiddingManager?)
}
