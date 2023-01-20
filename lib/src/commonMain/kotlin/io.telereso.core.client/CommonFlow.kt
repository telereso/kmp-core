package io.telereso.core.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A wrapper class for Kotlin Coroutine Flows for iOS.
 * This is picked up from jetbrain's kotlinconf FlowUtils.
 * So instead of flow, we only have callbacks that looks almost like a flow,
 * It also exposes a Closeable. that is responsible for closing any scope we have on Kotlin side. so client can simple call close().
 * this creates a lifecycle flow we can deal with.
 * it enables iOS to consume the flow data in a natural way.
 * refer to the iOS sample for how to use.
 * launchIn defines where we running our coroutine. here its running on Dispatchers.Main so this may cause issue with
 * Threading controlled on Kotlin KMM. e.g in case the client want to run this flow in a Dispatchers background it will still run on Main. but will explorer this some other time.
 *
 * Added error handling
 * If the Flow is wrapped in a Task and using CoroutineExceptionHandler we can grab the throws without crash
 * but what if the Flow is not using a wrapper, it crashes the app.
 * Here I added a way to catch any errors during the streaming of flows and send it as a callback
 * to the client. Note: on Android it will still crash unless we handled the try catch or CoroutineExceptionHandler
 * and on iOS we are able to get a error value with readable Exception of what went wrong.
 * On Android we can still use collect instead of watch
 */
class CommonFlow<T>(private val origin: Flow<T>) : Flow<T> by origin {
    /**
     * similar to calling collect{}.
     * use watch to collect flow
     */
    fun watch(block: (T?, ClientException?) -> Unit) {
        val job = Job()
        onEach {
            block(it, null)
        }.catch { error: Throwable ->
            ClientException.listener(error.toClientException())
            // Only pass on Exceptions.
            // This also correctly converts Exception to Swift Error.
            if (error is ClientException) {
                block(null, error)
            }
            throw error // Then propagate exeption on Kotlin code.

        }.launchIn(CoroutineScope(DispatchersProvider.Default + job))
    }
}

fun <T> Flow<T>.asCommonFlow(): CommonFlow<T> = CommonFlow(this)