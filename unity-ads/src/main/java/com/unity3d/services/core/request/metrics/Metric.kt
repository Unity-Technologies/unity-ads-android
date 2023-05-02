package com.unity3d.services.core.request.metrics

data class Metric @JvmOverloads constructor(val name: String?, val value: Any? = null, val tags: Map<String, String> = emptyMap()) {
    fun toMap(): Map<String, Any> {
        return buildMap {
            if (name != null) {
                put(METRIC_NAME, name)
            }
            if (value != null) {
                put(METRIC_VALUE, value)
            }
            if (tags.isNotEmpty()) {
                put(METRIC_TAGS, tags)
            }
        }
    }

    companion object {
        private const val METRIC_NAME = "n"
        private const val METRIC_VALUE = "v"
        private const val METRIC_TAGS = "t"
    }
}