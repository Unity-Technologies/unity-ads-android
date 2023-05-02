package com.unity3d.services.core.device

interface VolumeChange {
    fun startObserving()
    fun stopObserving()
    fun registerListener(volumeChangeListener: VolumeChangeListener)
    fun unregisterListener(volumeChangeListener: VolumeChangeListener)
    fun clearAllListeners()
}