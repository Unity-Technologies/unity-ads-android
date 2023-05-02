package com.unity3d.services.core.device.pii

import com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_ALT_KEY
import com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_KEY
import com.unity3d.services.core.device.reader.pii.NonBehavioralFlag
import com.unity3d.services.core.device.reader.pii.NonBehavioralFlagReader
import com.unity3d.services.core.misc.IJsonStorageReader
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NonBehavioralFlagReaderTest {
    @MockK
    lateinit var jsonStorageReaderMock: IJsonStorageReader

    @InjectMockKs
    lateinit var nonBehavioralFlagReader: NonBehavioralFlagReader

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_KEY) } returns null;
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_ALT_KEY) } returns null;
    }

    @Test
    fun getUserNonBehavioralFlag_dataMissing_flagUnknown() {
        // given
        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.UNKNOWN, nonBehavioralFlag)
    }

    @Test
    fun getUserNonBehavioralFlag_nonBehavioralTrue_flagTrue() {
        // given
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_KEY) } returns true;

        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.TRUE, nonBehavioralFlag)
    }

    @Test
    fun getUserNonBehavioralFlag_nonBehavioralTrueStr_flagTrue() {
        // given
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_KEY) } returns "True";

        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.TRUE, nonBehavioralFlag)
    }

    @Test
    fun getUserNonBehavioralFlag_nonBehavioralAltTrue_flagTrue() {
        // given
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_ALT_KEY) } returns true;

        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.TRUE, nonBehavioralFlag)
    }

    @Test
    fun getUserNonBehavioralFlag_nonBehavioralFalse_flagFalse() {
        // given
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_KEY) } returns false;

        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.FALSE, nonBehavioralFlag)
    }

    @Test
    fun getUserNonBehavioralFlag_nonBehavioralFalseStr_flagFalse() {
        // given
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_KEY) } returns "false";

        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.FALSE, nonBehavioralFlag)
    }

    @Test
    fun getUserNonBehavioralFlag_nonBehavioralInvalid_flagUnknown() {
        // given
        every { jsonStorageReaderMock.get(USER_NON_BEHAVIORAL_VALUE_KEY) } returns "invalid";

        // when
        val nonBehavioralFlag = nonBehavioralFlagReader.getUserNonBehavioralFlag()

        // then
        Assert.assertEquals(NonBehavioralFlag.UNKNOWN, nonBehavioralFlag)
    }
}