package com.wds.ads.test.unit;

import com.wds.ads.misc.Utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilitiesTest {
    @Test
    public void testSha256() throws Exception {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Utilities.Sha256(""));
    }
}