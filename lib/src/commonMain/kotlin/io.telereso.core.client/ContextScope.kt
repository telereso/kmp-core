package io.telereso.core.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ContextScope(
    context: CoroutineContext
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = context
    override fun toString(): String =
        "CoroutineScope(coroutineContext=)"

    companion object{
        fun get(
            context: CoroutineContext
        ): ContextScope =
            ContextScope(
                if (context[Job] != null) context
                else context + Job()
            )
    }
}
