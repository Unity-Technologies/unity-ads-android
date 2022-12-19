package com.unity3d.services.core.di

/**
 * Base interface to extend to facilitate access to [ServiceProvider]
 */
interface ServiceComponent {
    fun getServiceProvider(): ServiceProvider
}

/**
 * Get instance from the ServiceProvider
 * @param named if a specific instance is required
 *
 * @return instance of type T
 */
inline fun <reified T : Any> ServiceComponent.get(
    named: String = ""
): T {
    return getServiceProvider().getRegistry().getService<T>(named, instance = T::class)
}

/**
 * Lazy inject instance from the ServiceProvider
 * @param named if a specific instance is required
 * @param mode to define a specific [LazyThreadSafetyMode]
 *
 * @return [Lazy] instance of type T
 */
inline fun <reified T : Any> ServiceComponent.inject(
    named: String = "",
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
): Lazy<T> =
    lazy(mode) { get<T>(named) }