package com.unity3d.services.core.device

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.unity3d.services.core.properties.ClientProperties

class VolumeChangeContentObserver : VolumeChange {
    private var contentObserver: ContentObserver? = null
    private var listeners = mutableListOf<VolumeChangeListener>()
    @Synchronized
    override fun startObserving() {
        if (contentObserver != null) return
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun deliverSelfNotifications(): Boolean {
                return false
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                triggerListeners()
            }
        }
        val context = ClientProperties.getApplicationContext()
        context?.contentResolver?.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            contentObserver as ContentObserver
        )

    }

    @Synchronized
    override fun stopObserving() {
        if (contentObserver == null) return
        val context = ClientProperties.getApplicationContext()
        context?.contentResolver?.unregisterContentObserver(contentObserver!!)
        contentObserver = null
    }

    @Synchronized
    override fun registerListener(volumeChangeListener: VolumeChangeListener) {
        if (volumeChangeListener !in listeners) {
            startObserving()
            listeners.add(volumeChangeListener)
        }
    }

    @Synchronized
    override fun unregisterListener(volumeChangeListener: VolumeChangeListener) {
        listeners.remove(volumeChangeListener)
        if (listeners.isEmpty()) {
            stopObserving()
        }
    }

    @Synchronized
    override fun clearAllListeners() {
        listeners.clear()
        stopObserving()
    }

    @Synchronized
    private fun triggerListeners() {
        for (listener in listeners) {
            val currentVolume = Device.getStreamVolume(listener.getStreamType())
            listener.onVolumeChanged(currentVolume)
        }
    }
}