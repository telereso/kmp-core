/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.telereso.kmp.core

import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.asClientException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

/**
 * A wrapper class for Kotlin Coroutine Flows for iOS.
 * This is picked up from jetbrain's kotlinconf FlowUtils.
 * So instead of flow, we only have callbacks that looks almost like a flow,
 * It also exposes a Closeable. that is responsible for closing any scope we have on Kotlin side. so client can simple call close().
 * this creates a lifecycle flow we can deal with.
 * it enables iOS and web to consume the flow data in a natural way.
 * refer to the iOS and web samples for how to use.
 * launchIn defines where we running our coroutine. here its running on Dispatchers.Main so this may cause issue with
 * Threading controlled on Kotlin KMM. e.g in case the client want to run this flow in a Dispatchers background it will still run on Main. but will explorer this some other time.
 *
 * If the Flow is wrapped in a Task and using CoroutineExceptionHandler we can grab the throws without crash
 * but what if the Flow is not using a wrapper, it's causing a crashes.
 * Here we added a way to catch any errors during the streaming of flows and send it as a callback
 * to the client.
 *
 * Note: Android should use collect instead of watch and handle errors by using try/catch or CoroutineExceptionHandler
 * and on iOS adn web we are able to get a error value with readable Exception of what went wrong.
 *
 */
// @JsExport
class CommonFlow<T>(private val origin: Flow<T>) : Flow<T> by origin {

    class Job internal constructor(private val collectJob: kotlinx.coroutines.Job) {
        /**
         * Platforms : iOS, JS
         * Use to cancel the collecting job if used [watch]
         */
        fun cancel(){
            runCatching { collectJob.cancel() }.getOrElse { ClientException.listener.invoke(it) }
        }
    }
    /**
     * similar to calling collect{}.
     * use watch to collect flow
     */
    fun watch(stream: (T?, ClientException?) -> Unit): Job {
        val job = Job()
        val commonFlowJob = Job(job)
        onEach {
            stream(it, null)
        }.catch { error: Throwable ->
            ClientException.listener(error.asClientException())
            // Only pass on Exceptions.
            // This also correctly converts Exception to Swift Error.
            if (error is ClientException) {
                stream(null, error)
            }
            throw error // Then propagate exception on Kotlin code.

        }.launchIn(CoroutineScope(DispatchersProvider.Default + job))
        return commonFlowJob
    }
}

fun <T> Flow<T>.asCommonFlow(): CommonFlow<T> = CommonFlow(this)


object Flows {
    @JvmStatic
    fun <T> from(list:List<T>): CommonFlow<T> {
        return list.asFlow().asCommonFlow()
    }

    @JvmStatic
    fun <T> fromArray(array:Array<T>): CommonFlow<T> {
        return array.asFlow().asCommonFlow()
    }
}