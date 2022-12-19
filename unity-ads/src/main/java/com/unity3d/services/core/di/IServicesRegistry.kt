package com.unity3d.services.core.di

import kotlin.reflect.KClass

/**
 * Class responsible for instantiating and containment of instances
 */
interface IServicesRegistry {

    val services: Map<ServiceKey, Lazy<*>>

    fun <T> updateService(key: ServiceKey, instance: Lazy<T>)

    fun <T> resolveService(key: ServiceKey): T

    fun <T> resolveServiceOrNull(key: ServiceKey): T?

    fun <T> getService(named: String = "", instance: KClass<*>): T

}