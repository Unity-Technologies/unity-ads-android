package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.domain.ISDKDispatchers
import com.unity3d.services.core.extensions.runReturnSuspendCatching
import kotlinx.coroutines.withContext

class InitializeStateRetry(
    private val dispatchers: ISDKDispatchers,
) : BaseTask<InitializeStateRetry.Params, Result<Unit>> {

    override suspend fun doWork(params: Params): Result<Unit> = withContext(dispatchers.default) {
        runReturnSuspendCatching {
            // interactor logic 
        }
    }

    data class Params(val config: Configuration) : BaseParams
}