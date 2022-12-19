package com.unity3d.services.core.di

/**
 * Service provider based on Koin Apis
 */
interface IServiceProvider {

    fun initialize(): IServicesRegistry

    fun getRegistry(): IServicesRegistry

}