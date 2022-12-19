package com.unity3d.services.core.di

private class Factory<out T> internal constructor(private val initializer: () -> T) : Lazy<T> {

    override val value: T
        get() {
            return initializer()
        }

    override fun isInitialized(): Boolean = false
}

fun <T> factoryOf(initializer: () -> T): Lazy<T> = Factory(initializer)