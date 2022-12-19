package com.unity3d.services.core.domain.task

import com.unity3d.services.core.configuration.Configuration
import com.unity3d.services.core.configuration.ErrorState

data class InitializationException(val errorState: ErrorState, val originalException: Exception, val config: Configuration): Exception(originalException)