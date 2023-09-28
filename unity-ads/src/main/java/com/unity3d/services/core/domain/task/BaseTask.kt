package com.unity3d.services.core.domain.task

import com.unity3d.services.core.di.IServiceComponent

/**
 * A task represents an execution unit of asynchronous work.
 * A [BaseTask] returns a single response through a suspend function.
 */
interface BaseTask<in P: BaseParams, R>: IServiceComponent {

    suspend operator fun invoke(params: P): Result<R> = doWork(params)

    suspend fun doWork(params: P): Result<R>

}