package com.unity3d.services.core.device

import com.unity3d.services.core.preferences.AndroidPreferences
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Test
import kotlin.test.assertEquals

class DeviceTest {
    @Test
    fun getAuid_auidProvided_auidReturned() {
        mockkStatic(AndroidPreferences::class) {
            // given
            every { AndroidPreferences.getString("supersonic_shared_preferen", "auid") } returns "fakeAuid";

            // when
            val idfi = Device.getAuid();

            // then
            assertEquals("fakeAuid", idfi)
        }
    }

    @Test
    fun getIdfi_auidProvidedIdfiMissing_auidReturned() {
        mockkStatic(AndroidPreferences::class) {
            // given
            every { AndroidPreferences.getString("unityads-installinfo", "unityads-idfi") } returns null;
            every { AndroidPreferences.getString("supersonic_shared_preferen", "auid") } returns "fakeAuid";

            // when
            val idfi = Device.getIdfi();

            // then
            assertEquals("fakeAuid", idfi)
        }
    }

    @Test
    fun getIdfi_auidMissing_idfiReturned() {
        mockkStatic(AndroidPreferences::class) {
            // given
            every { AndroidPreferences.getString("unityads-installinfo", "unityads-idfi") } returns "fakeIdfi";
            every { AndroidPreferences.getString("supersonic_shared_preferen", "auid") } returns null;

            // when
            val idfi = Device.getIdfi();

            // then
            assertEquals("fakeIdfi", idfi)
        }
    }

    @Test
    fun getIdfi_bothMissing_idfiGeneratedAndReturned() {
        mockkStatic(AndroidPreferences::class) {
            // given
            every { AndroidPreferences.getString("unityads-installinfo", "unityads-idfi") } returns null;
            every { AndroidPreferences.getString("supersonic_shared_preferen", "auid") } returns null;
            every { AndroidPreferences.setString(any(), any(), any()) } returns Unit;

            // when
            val idfi = Device.getIdfi();

            // then
            assertEquals(36, idfi.length)
        }
    }
}