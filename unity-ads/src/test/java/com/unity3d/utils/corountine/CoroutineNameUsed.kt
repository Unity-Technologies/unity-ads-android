package com.unity3d.utils.corountine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class CoroutineNameUsed(
    val name: String
) : ThreadContextElement<Boolean>, AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<CoroutineNameUsed>

    override val key = Key

    var isUsed = false

    override fun updateThreadContext(context: CoroutineContext): Boolean {
        val contextName = context[CoroutineName.Key]
        if (contextName?.name.equals(name)) {
            isUsed = true
        }
        return isUsed
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Boolean) {
        return
    }
}

suspend fun coroutineNameIsUsed(): Boolean {
    return coroutineContext[CoroutineNameUsed]?.isUsed ?: false
}