package com.unity3d.services.core.device.reader.pii

enum class NonBehavioralFlag {
    UNKNOWN,
    TRUE,
    FALSE;

    companion object {
        fun fromString(value: String): NonBehavioralFlag =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}