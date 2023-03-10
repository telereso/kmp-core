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
import kotlinx.coroutines.*
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * A discriminated union that encapsulates a successful outcome with a value of type [ResultT] or a failure with an arbitrary Throwable [ClientException].
 * @param scope a CoroutineScope defaulted to Default can provide your own scope as well, ensure its testable by injecting the provider.
 */
@ExperimentalJsExport
@JsExport
class Task<ResultT> private constructor(
    private val scope: CoroutineScope,
    block: suspend CoroutineScope.() -> ResultT
) {
    private var _task = InternalTask(this)

    /**
     * Can be used to assign the task job while doing unit testing,
     * not meant to be exposed or used in actual logic
     */
    internal val job: Deferred<ResultT> =
        scope.async(block = block)

    init {
        val handler = CoroutineExceptionHandler { _, exception ->
            failure?.invoke(exception.asClientException())
        }
        scope.launch(handler) {
            val res = job.await()
            success?.invoke(res)
        }
    }

    /**
     * This scope is used only created and used when invoking [successUI] and [failureUI]
     */
    private val scopeUI: CoroutineScope by lazy {
        ContextScope.get(DispatchersProvider.Main)
    }

    /**
     * using the Task's instance, we can call success.invoke and passing a success result
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private var success: ((ResultT) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCompleted && !job.isCancelled) {
                    value.invoke(job.getCompleted())
                }
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private var successUI: ((ResultT) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCompleted && !job.isCancelled) {
                    scopeUI.launch {
                        value.invoke(job.getCompleted())
                    }
                }
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    /**
     * using the Task's instance, we can call failure.invoke passing in a preferred Throwable [ClientException] as a failure result
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private var failure: ((ClientException) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCompleted)
                    job.getCompletionExceptionOrNull()?.let {
                        if (it !is CancellationException)
                            value.invoke(it.asClientException())
                    }
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private var failureUI: ((ClientException) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCompleted)
                    job.getCompletionExceptionOrNull()?.let {
                        if (it !is CancellationException)
                            scopeUI.launch {
                                value.invoke(it.asClientException())
                            }
                    }
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    /**
     * using the Task's instance, we can cancel and running coroutines of this Task's scope.
     * alternatively we can use the Task's cancel fun @see [Task.cancel]
     */
    @OptIn(InternalCoroutinesApi::class)
    private var cancelTask: ((ClientException) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCancelled)
                    value.invoke(job.getCancellationException().asClientException())
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    /**
     * a success scope that is attachable to a task instance
     *      *  task().onSuccess {
     *     // do background logic , call another api
     *  }
     *  use this scope to  retrieve the success response of a task
     */
    fun onSuccess(action: (ResultT) -> Unit): Task<ResultT> {

        // Ignore duplicate success & successUI calls and accept the first pair only
        if (success != null && successUI != null) return this

        success = { res ->
            action(res)
            successUI?.let {
                scopeUI.launch(DispatchersProvider.Main) {
                    it.invoke(res)
                }
            }
        }
        return this
    }

    /**
     * a failure scope that is attachable to a task instance
     *      *  task().onFailure {
     *     // handle failure
     *  }
     *  use this scope to retrieve the failure response of a task
     */
    fun onFailure(action: (ClientException) -> Unit): Task<ResultT> {

        // Ignore duplicate failure & failureUI calls and accept the first pair only
        if (failure != null && failureUI != null) return this

        failure = { e ->
            action(e)
            failureUI?.let {
                scopeUI.launch(DispatchersProvider.Main) {
                    it.invoke(e)
                }
            }
        }
        return this
    }

    /**
     * a cancel scope that is attachable to a task instance
     *      *  task().onCancel {
     *     // Do this on cancel
     *  }
     *  use this scope to listen on the cancel response of a task
     */
    fun onCancel(action: (ClientException) -> Unit): Task<ResultT> {
        cancelTask = { e ->
            action(e)
        }
        return this
    }

    /**
     * Experimental callback to avoid letting switch threads,
     * Also allow a chance to perform UI and background jobs after a task is done without dealing with multithreading
     * ```
     *  callApi().onSuccess {
     *     // do background logic , call another api
     *  }.onSuccessUI {
     *     // do UI logic , update TextViews ..etc
     *  }
     * ```
     * if it did not work , or was miss used we can remove
     */
    fun onSuccessUI(action: (ResultT) -> Unit): Task<ResultT> {

        // ignore duplicate success & successUI calls and accept the first pair only
        if (success != null && successUI != null) return this

        val jobIsCompleted = job.isCompleted
        successUI = action

        if (success == null) {
            success = { res ->
                if (!jobIsCompleted)
                    scopeUI.launch(DispatchersProvider.Main) {
                        successUI?.invoke(res)
                    }
            }
        }
        return this

    }

    /**
     * Experimental callback to avoid adding switch threads logic,
     * Also allow a chance to perform UI and background jobs after a task is done without dealing with multithreading
     * ```
     *  callApi().onFailure {
     *     // do background logic , call another api
     *  }.onFailureUI {
     *     // do UI logic , update TextViews ..etc
     *  }
     * ```
     * example usage in client Manager. ensure to use task.failureUI instead of task.failure else the scope will never succeed
     * if it did not work , or was miss used we can remove
     */
    fun onFailureUI(action: (ClientException) -> Unit): Task<ResultT> {

        // ignore duplicate failure & failureUI calls and accept the first pair only
        if (failure != null && failureUI != null) return this

        val jobIsCompleted = job.isCompleted
        failureUI = action

        if (failure == null) {
            failure = { e ->
                if (!jobIsCompleted)
                    scopeUI.launch(DispatchersProvider.Main) {
                        action.invoke(e)
                    }
            }
        }
        return this
    }

    /**
     * cancels the current task's scope and invokes a cancelTask.
     *
     */
    fun cancel(message: String, throwable: ClientException? = null) {
        val error = throwable ?: ClientException(message)
        scope.cancel(message, error)
        cancelTask?.invoke(error)
    }

    /**
     * Wait for the task to finish
     * it is a blocking call, if using it inside coroutine or suspended function use [await] instead
     * @return [ResultT] if succeeded , or crash if job failed, if you don't care about resultT check [getOrNull]
     */
    @RunBlocking
    fun get(): ResultT {
        return _task.get()
    }

    /**
     * Wait for the task to finish
     * it is a blocking call, if using it inside coroutine or suspended function use [awaitOrNull] instead
     * @return [ResultT] if succeeded or null if failed
     */
    @RunBlocking
    fun getOrNull(): ResultT? {
        return _task.getOrNull()
    }

    class Builder {
        private var scope: CoroutineScope? = null

        /**
         * provide your own scope for the task to run on
         */
        fun withScope(scope: CoroutineScope = ContextScope.getSupervisor(DispatchersProvider.Default)): Builder {
            this.scope = scope
            return this
        }

        /**
         * @param block provide your logic
         */
        fun <ResultT> execute(
            block: suspend CoroutineScope.() -> ResultT
        ): Task<ResultT> {
            return Task(
                scope ?: ContextScope.getSupervisor(
                    DispatchersProvider.Default
                ), block
            )
        }
    }

    companion object {
        /**
         * Build that will create a task and it's logic
         * @param block That task logic
         */
        inline fun <ResultT> execute(
            noinline block: suspend CoroutineScope.() -> ResultT
        ): Task<ResultT> {
            return Builder().withScope().execute(block)
        }
    }
}

internal expect class InternalTask<ResultT>(_task: Task<ResultT>) {

    internal val task: Task<ResultT>

    @RunBlocking
    fun get(): ResultT

    @RunBlocking
    fun getOrNull(): ResultT?
}


/**
 * Wait for the task to finish
 * @return [ResultT] if succeeded , or crash if job failed ,if you don't care about resultT check [awaitOrNull]
 */
suspend fun <ResultT> Task<ResultT>.await(): ResultT {
    return job.await()
}


/**
 * Wait for the task to finish
 * @return [ResultT] if succeeded or null if failed
 */
suspend fun <ResultT> Task<ResultT>.awaitOrNull(): ResultT? {
    return try {
        job.await()
    } catch (t: Throwable) {
        ClientException.listener.invoke(t)
        null
    }
}