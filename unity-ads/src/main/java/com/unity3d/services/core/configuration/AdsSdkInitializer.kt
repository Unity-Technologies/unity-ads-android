package com.unity3d.services.core.configuration

import android.content.Context
import androidx.startup.Initializer
import com.unity3d.services.core.properties.ClientProperties

/**
 * Unity SDK Initializer implementation of Androidx.Startup - Initializer interface.
 */
class AdsSdkInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        ClientProperties.setApplicationContext(context.applicationContext)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}