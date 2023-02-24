package com.unity3d.services.core.cache

import junit.framework.TestCase.assertNull
import org.junit.Test

class CacheDirectoryTest {
    @Test
    fun getCacheDirectory_nullContext_nullFile() {
        // given
        val testContext = null

        // when
        val cacheDirectory = CacheDirectory("")
        val cacheDirectoryFile = cacheDirectory.getCacheDirectory(testContext)

        // then
        assertNull(cacheDirectoryFile)
    }
}