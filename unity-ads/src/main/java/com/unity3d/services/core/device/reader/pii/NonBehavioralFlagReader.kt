package com.unity3d.services.core.device.reader.pii

import com.unity3d.services.core.device.reader.JsonStorageKeyNames
import com.unity3d.services.core.misc.IJsonStorageReader

open class NonBehavioralFlagReader(private val jsonStorageReader: IJsonStorageReader) {
    open fun getUserNonBehavioralFlag(): NonBehavioralFlag {
        val privacyModeObj: Any? = jsonStorageReader.get(JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_KEY)
            ?: jsonStorageReader.get(JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_ALT_KEY)
            
        return NonBehavioralFlag.fromString(privacyModeObj.toString())
    }
}