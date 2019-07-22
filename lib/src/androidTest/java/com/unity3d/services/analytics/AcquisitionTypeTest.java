package com.unity3d.services.analytics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AcquisitionTypeTest {

    @Test
    public void testToString() {
        assertEquals("premium", AcquisitionType.PREMIUM.toString());
        assertEquals("soft", AcquisitionType.SOFT.toString());
    }

}
