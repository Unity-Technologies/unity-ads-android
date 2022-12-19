package com.unity3d.services.core.di

import kotlin.reflect.KClass

data class ServiceKey(
    val named: String = "",
    val instanceClass: KClass<*>,
)