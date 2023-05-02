package com.unity3d.services.core.device

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithAuid
import com.unity3d.services.core.device.reader.IDeviceInfoReader
import com.unity3d.services.core.device.reader.JsonStorageKeyNames
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeviceInfoReaderWithAuidTest {
    @MockK
    private lateinit var deviceInfoReader: IDeviceInfoReader

    @InjectMockKs
    private lateinit var deviceInfoReaderWithAuid: DeviceInfoReaderWithAuid

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun getDeviceInfoData_auidProvided_auidPresent() {
        mockkStatic(Device::class) {
            // given
            every { deviceInfoReader.deviceInfoData } returns mutableMapOf("key" to "value") as Map<String, Any>?
            every { Device.getAuid() } returns "fakeAuid";

            // when
            val deviceInfoData = deviceInfoReaderWithAuid.deviceInfoData

            // then
            Assert.assertEquals("fakeAuid", deviceInfoData[JsonStorageKeyNames.AUID_ID_KEY])
            Assert.assertNotNull(deviceInfoData["key"])
        }
    }

    @Test
    fun getDeviceInfoData_auidMissing_auidMissingFromData() {
        mockkStatic(Device::class) {
            // given
            every { Device.getAuid() } returns null;

            // when
            val deviceInfoData = deviceInfoReaderWithAuid.deviceInfoData

            // then
            Assert.assertNull(deviceInfoData[JsonStorageKeyNames.AUID_ID_KEY])
        }
    }
}