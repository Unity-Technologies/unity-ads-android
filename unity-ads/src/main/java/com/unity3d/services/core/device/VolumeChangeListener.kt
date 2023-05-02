package com.unity3d.services.core.device

interface VolumeChangeListener {
    fun onVolumeChanged(volume: Int)
    fun getStreamType(): Int
}