package com.unity3d.services.core.device

import android.util.SparseArray
import com.unity3d.services.core.webview.WebViewEventCategory
import com.unity3d.services.core.webview.bridge.IEventSender
import com.unity3d.services.core.webview.bridge.SharedInstances

class VolumeChangeMonitor(private val eventSender: IEventSender, private val volumeChange: VolumeChange) {
    private val volumeChangeListeners: SparseArray<VolumeChangeListener> = SparseArray()

    fun registerVolumeChangeListener(streamType: Int) {
        if (volumeChangeListeners[streamType] == null) {
            val listener = object : VolumeChangeListener {
                override fun onVolumeChanged(volume: Int) {
                    eventSender.sendEvent(
                        WebViewEventCategory.DEVICEINFO,
                        DeviceInfoEvent.VOLUME_CHANGED,
                        getStreamType(),
                        volume,
                        Device.getStreamMaxVolume(streamType)
                    )
                }

                override fun getStreamType(): Int {
                    return streamType
                }
            }
            volumeChangeListeners.append(streamType, listener)
            volumeChange.registerListener(listener)
        }
    }

    fun unregisterVolumeChangeListener(streamType: Int) {
        if (volumeChangeListeners[streamType] != null) {
            val listener = volumeChangeListeners[streamType]
            volumeChange.unregisterListener(listener)
            volumeChangeListeners.remove(streamType)
        }
    }
}
