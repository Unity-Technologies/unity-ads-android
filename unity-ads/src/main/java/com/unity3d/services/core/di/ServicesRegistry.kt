@file:Suppress("UNCHECKED_CAST")

package com.unity3d.services.core.di

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Contains a collection of all instances required by a [IServiceProvider]
 */
class ServicesRegistry internal constructor() : IServicesRegistry {

    private val _services = ConcurrentHashMap<ServiceKey, Lazy<*>>()
    override val services: Map<ServiceKey, Lazy<*>>
        get() = _services

    /**
     * Creates a singleton instance of a service and adds id to the services map
     *
     * @param named is a unique id for the service if multiple implementations exist
     */
    inline fun <reified T> single(named: String = "", noinline instance: () -> T): ServiceKey {
        val key = ServiceKey(named, T::class)
        val lazyInstance = lazy(instance)
        updateService(key, lazyInstance)
        return key
    }

    /**
     * Creates a new instance of a service each time it is requested
     *
     * @param named is a unique key for the service if multiple implementations exist
     */
    inline fun <reified T> factory(named: String = "", noinline instance: () -> T): ServiceKey {
        val key = ServiceKey(named, T::class)
        val lazyInstance = factoryOf(instance)
        updateService(key, lazyInstance)
        return key
    }

    /**
     * Updates the services map with passed in [ServiceKey]
     *
     * @param key the [ServiceKey] to be added to the map
     * @param instance the [Lazy] instance of the service
     */
    override fun <T> updateService(key: ServiceKey, instance: Lazy<T>) {
        check(!services.containsKey(key)) { "Cannot have multiple identical services" }
        _services[key] = instance as Lazy<*>
    }

    /**
     * Retrieves a service from the services map
     *
     * @param named is a unique key for the service if multiple implementations exist
     */
    inline fun <reified T> get(named: String = ""): T {
        val key = ServiceKey(named, T::class)
        return resolveService(key)
    }

    /**
     * Retrieves a service from the services map
     *
     * @param named is a unique key for the service if multiple implementations exist
     */
    override fun <T> getService(named: String, instance: KClass<*>): T {
        val key = ServiceKey(named, instance)
        return resolveService(key)
    }

    /**
     * Retrieves a service from the services map or returns null if not found
     *
     * @param named is a unique key for the service if multiple implementations exist
     */
    inline fun <reified T> getOrNull(named: String = ""): T? {
        val key = ServiceKey(named, T::class)
        return resolveServiceOrNull(key)
    }

    /**
     * Attempts to fetch a service instance
     * or throws [IllegalStateException] if it does not exist
     *
     * @param key the [ServiceKey] to be fetched to the map
     */
    override fun <T> resolveService(key: ServiceKey): T {
        val lazy = services[key] ?: throw IllegalStateException("No service instance found for $key")
        return lazy.value as T
    }

    /**
     * Attempts to fetch a service instance
     * or returns null if it does not exist
     *
     * @param key the [ServiceKey] to be fetched to the map
     */
    override fun <T> resolveServiceOrNull(key: ServiceKey): T? {
        val lazy = services[key] ?: return null
        return lazy.value as T
    }

}

fun registry(registry: ServicesRegistry.() -> Unit): ServicesRegistry {
    return ServicesRegistry().apply(registry)
}