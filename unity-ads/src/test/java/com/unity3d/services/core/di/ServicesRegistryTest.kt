package com.unity3d.services.core.di

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before

import org.junit.Test

class ServicesRegistryTest {

    // Class under test
    lateinit var servicesRegistry: ServicesRegistry

    @Before
    fun setUp() {
        servicesRegistry = ServicesRegistry()
    }

    @Test
    fun single_createInstance_instanceExists() {
        servicesRegistry.single<Int> { 3 }

        val value = servicesRegistry.get<Int>()

        assertEquals(3, value)
    }

    @Test(expected = IllegalStateException::class)
    fun single_createVariousSameInstances_throws() {
        servicesRegistry.single<Int> { 3 }
        servicesRegistry.single<Int> { 3 }
    }

    @Test
    fun single_createVariousSameNamedInstances_instancesExists() {
        servicesRegistry.single<Int> { 3 }
        servicesRegistry.single<Int>("3") { 3 }
        servicesRegistry.single<Int>("three") { 3 }

        val value = servicesRegistry.get<Int>()
        val value3 = servicesRegistry.get<Int>(named = "3")
        val valueThree = servicesRegistry.get<Int>(named = "three")

        assertEquals(3, value)
        assertEquals(3, value3)
        assertEquals(3, valueThree)
        assertEquals(value, value3)
        assertEquals(value3, valueThree)
    }

    @Test
    fun single_createOneInstance_getsSameInstances() {
        servicesRegistry.single<MutableList<Int>> { mutableListOf(3) }

        val value1 = servicesRegistry.get<MutableList<Int>>()
        value1.add(4)
        val value2 = servicesRegistry.get<MutableList<Int>>()

        assertSame(value1, value2)
    }

    @Test
    fun factory_createInstance_instanceExists() {
        servicesRegistry.factory<Int> { 3 }

        val value = servicesRegistry.get<Int>()

        assertEquals(3, value)
    }

    @Test(expected = IllegalStateException::class)
    fun factory_createVariousSameInstances_throws() {
        servicesRegistry.factory<Int> { 3 }
        servicesRegistry.factory<Int> { 3 }
    }

    @Test
    fun factory_createVariousSameNamedInstances_instancesExists() {
        servicesRegistry.factory<Int> { 3 }
        servicesRegistry.factory<Int>("3") { 3 }
        servicesRegistry.factory<Int>("three") { 3 }

        val value = servicesRegistry.get<Int>()
        val value3 = servicesRegistry.get<Int>(named = "3")
        val valueThree = servicesRegistry.get<Int>(named = "three")

        assertEquals(3, value)
        assertEquals(3, value3)
        assertEquals(3, valueThree)
        assertEquals(value, value3)
        assertEquals(value3, valueThree)
    }

    @Test
    fun factory_createOneInstance_getsDifferentInstances() {
        servicesRegistry.factory<MutableList<Int>> { mutableListOf(3) }

        val value1 = servicesRegistry.get<MutableList<Int>>()
        value1.add(4)
        val value2 = servicesRegistry.get<MutableList<Int>>()

        assertNotSame(value1, value2)
    }

    @Test(expected = IllegalStateException::class)
    fun get_noInstance_throws() {
        servicesRegistry.get<Int>()
    }

    @Test
    fun get_withInstance_returnsInstance() {
        servicesRegistry.single<Int> { 3 }

        val value = servicesRegistry.get<Int>()

        assertEquals(3, value)
    }

    @Test
    fun getOrNull_noInstance_returnsNull() {
        val value = servicesRegistry.getOrNull<Int>()

        assertNull(value)
    }

    @Test
    fun getOrNull_withInstance_returnsInstance() {
        servicesRegistry.single<Int> { 3 }

        val value = servicesRegistry.getOrNull<Int>()

        assertEquals(3, value)
    }


    @Test
    fun updateServices_withUniqueInstance_addedToServicesMap() {
        val key = ServiceKey("", Int::class)
        val lazyInstance = lazy { 3 }

        servicesRegistry.updateService(key, lazyInstance)

        assertTrue(servicesRegistry.services.containsKey(key))
    }

    @Test(expected = IllegalStateException::class)
    fun updateServices_withDuplicateInstance_throws() {
        val key = ServiceKey("", Int::class)
        val lazyInstance = lazy { 3 }

        servicesRegistry.updateService(key, lazyInstance)
        servicesRegistry.updateService(key, lazyInstance)
    }

    @Test
    fun updateServices_withDuplicateNameInstance_servicesAddedToServicesMap() {
        val key1 = ServiceKey("1", Int::class)
        val key2 = ServiceKey("2", Int::class)
        val lazyInstance = lazy { 3 }

        servicesRegistry.updateService(key1, lazyInstance)
        servicesRegistry.updateService(key2, lazyInstance)

        assertTrue(servicesRegistry.services.containsKey(key1))
        assertTrue(servicesRegistry.services.containsKey(key2))
    }

    @Test
    fun resolveService_serviceExists_returnsService() {
        val key = ServiceKey("", Int::class)
        val lazyInstance = lazy { 3 }

        servicesRegistry.updateService(key, lazyInstance)
        val value = servicesRegistry.resolveService<Int>(key)

        assertEquals(3, value)
    }

    @Test(expected = IllegalStateException::class)
    fun resolveService_serviceDoesNotExist_throws() {
        val key = ServiceKey("", Int::class)

        servicesRegistry.resolveService<Int>(key)
    }

    @Test
    fun resolveServiceOrNull_serviceExists_returnsService() {
        val key = ServiceKey("", Int::class)
        val lazyInstance = lazy { 3 }

        servicesRegistry.updateService(key, lazyInstance)
        val value = servicesRegistry.resolveServiceOrNull<Int>(key)

        assertEquals(3, value)
    }

    @Test
    fun resolveServiceOrNull_serviceDoesNotExist_returnsNull() {
        val key = ServiceKey("", Int::class)

        val value = servicesRegistry.resolveServiceOrNull<Int>(key)

        assertNull(value)
    }
}