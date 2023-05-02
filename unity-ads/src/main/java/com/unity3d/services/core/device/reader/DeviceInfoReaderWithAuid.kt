package com.unity3d.services.core.device.reader

import com.unity3d.services.core.device.Device

class DeviceInfoReaderWithAuid(
    private val _deviceInfoReader: IDeviceInfoReader
) : IDeviceInfoReader {
    override fun getDeviceInfoData(): Map<String, Any> {
        val originalData: MutableMap<String, Any> = _deviceInfoReader.deviceInfoData
        Device.getAuid()?.let { originalData[JsonStorageKeyNames.AUID_ID_KEY] = it }
        return originalData
    }
}