package com.unity3d.services.core.domain

import kotlinx.coroutines.CoroutineDispatcher

interface ISDKDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}