package com.unity3d.services.core.device.pii

import com.unity3d.services.core.device.reader.pii.NonBehavioralFlag
import org.junit.Assert
import org.junit.Test

class NonBehavioralFlagTest {
    @Test
    fun fromString_trueString_behavioralTrue() {
        // given
        // when
        val nonBehavioralFlag = NonBehavioralFlag.fromString("true")
        // then
        Assert.assertEquals(NonBehavioralFlag.TRUE, nonBehavioralFlag)
    }

    @Test
    fun fromString_falseString_behavioralFalse() {
        // given
        // when
        val nonBehavioralFlag = NonBehavioralFlag.fromString("false")
        // then
        Assert.assertEquals(NonBehavioralFlag.FALSE, nonBehavioralFlag)
    }

    @Test
    fun fromString_invalidString_behavioralUnknown() {
        // given
        // when
        val nonBehavioralFlag = NonBehavioralFlag.fromString("invalid")
        // then
        Assert.assertEquals(NonBehavioralFlag.UNKNOWN, nonBehavioralFlag)
    }
}