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

import io.telereso.kmp.annotations.Builder
import io.telereso.kmp.core.extensions.getOrDefault
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.toClientException
import kotlinx.coroutines.*
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic


/**
 * A discriminated union that encapsulates a successful outcome with a value of type [ResultT] or a failure with an arbitrary Throwable [ClientException].
 * @param scope a CoroutineScope defaulted to Default can provide your own scope as well, ensure its testable by injecting the provider.
 */
@ExperimentalJsExport
// @JsExport
class Task<ResultT> private constructor(
    private val scope: CoroutineScope,
    private val config: TaskConfig? = TaskConfig(),
    block: suspend CoroutineScope.() -> ResultT
) {

    private val internalTask = InternalTask(this)

    /**
     * Number of tires to run the task , this will configured using [TaskConfig.retry]
     */
    private var tries = 1

    /**
     * Can be used to assign the task job while doing unit testing,
     * not meant to be exposed or used in actual logic
     */
    internal var job: Deferred<ResultT> =
        scope.async(block = {
            val c = config ?: TaskConfig()
            if (c.startDelay.getOrDefault() > 0)
                delay(c.startDelay.getOrDefault().toLong())

            if (c.retry.getOrDefault() > 0) {
                var res: ResultT? = null
                while (res == null && tries <= c.retry.getOrDefault()) {
                    res = runCatching { block() }.getOrNull()
                    if (res != null)
                        break

                    if (c.backOffDelay.getOrDefault() > 0) {
                        delay(tries * c.backOffDelay.getOrDefault().toLong())
                    }
                    tries++
                }
                // return result or run the block and allow it to fail
                res ?: block()
            } else {
                block()
            }
        })

    init {
        val handler = CoroutineExceptionHandler { _, exception ->
            scope.launch {
                val e = exception.toClientException(tries)
                failure?.invoke(e) ?: complete?.invoke(null, e)
            }
        }
        scope.launch(handler) {
            val res = job.await()
            success?.invoke(res) ?: complete?.invoke(res, null)
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
                            scope.launch { value.invoke(it.toClientException(tries)) }
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
                                value.invoke(it.toClientException(tries))
                            }
                    }
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    /**
     * Using the Task's instance, we can call complete.invoke passing in a [ResultT] if job succeeded or preferred Throwable [ClientException] as a failure result
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private var complete: ((ResultT?, ClientException?) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCompleted) {
                    val e = job.getCompletionExceptionOrNull()
                    if (e == null)
                        value.invoke(job.getCompleted(), null)
                    else
                        scope.launch { value.invoke(null, e.toClientException(tries)) }
                }
            } catch (t: Throwable) {
                ClientException.listener.invoke(t)
            }
            field = value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private var completeUI: ((ResultT?, ClientException?) -> Unit)? = null
        set(value) {
            try {
                if (field == null && value != null && job.isCompleted) {
                    val e = job.getCompletionExceptionOrNull()
                    scopeUI.launch {
                        if (e == null)
                            value.invoke(job.getCompleted(), null)
                        else
                            value.invoke(null, e.toClientException(tries))
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
                    scope.launch {
                        value.invoke(job.getCancellationException().toClientException(tries))
                    }
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
            complete?.invoke(res, null)
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
            complete?.invoke(null, e)
            failureUI?.let {
                scopeUI.launch(DispatchersProvider.Main) {
                    it.invoke(e)
                }
            }
        }
        return this
    }

    /**
     * a task finished scope that is attachable to a task instance
     *      *  task().onComplete {res, e ->
     *     // handle task completed
     *  }
     *  Use this scope to retrieve the end result of a task with a [ResultT] if succeed or [ClientException] if failed
     */
    fun onComplete(action: (ResultT?, ClientException?) -> Unit): Task<ResultT> {

        // Ignore duplicate complete & completeUI calls and accept the first pair only
        if (complete != null && completeUI != null) return this

        complete = { res, e ->
            action(res, e)
            completeUI?.let {
                scopeUI.launch(DispatchersProvider.Main) {
                    it.invoke(res, e)
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
            complete?.invoke(null, e)
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
     * Experimental callback to avoid adding switch threads logic,
     * Also allow a chance to perform UI and background jobs after a task is done without dealing with multithreading
     * ```
     *  callApi().onComplete {
     *     // do background logic , call another api
     *  }.onCompleteUI {
     *     // do UI logic , update TextViews ..etc
     *  }
     * ```
     */
    fun onCompleteUI(action: (ResultT?, ClientException?) -> Unit): Task<ResultT> {

        // ignore duplicate failure & failureUI calls and accept the first pair only
        if (complete != null && completeUI != null) return this

        val jobIsCompleted = job.isCompleted
        completeUI = action

        if (complete == null) {
            complete = { res, e ->
                if (!jobIsCompleted)
                    scopeUI.launch(DispatchersProvider.Main) {
                        action.invoke(res, e)
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
        cancelTask?.invoke(error) ?: complete?.invoke(null, error)
    }

    /**
     * Wait for the task to finish
     * it is a blocking call, if using it inside coroutine or suspended function use [await] instead
     * @return [ResultT] if succeeded , or crash if job failed, if you don't care about resultT check [getOrNull]
     */
    @RunBlocking
    @Throws(Exception::class)
    fun get(): ResultT {
        return internalTask.get()
    }

    /**
     * Wait for the task to finish
     * it is a blocking call, if using it inside coroutine or suspended function use [awaitOrNull] instead
     * @return [ResultT] if succeeded or null if failed
     */
    @RunBlocking
    @Throws(Exception::class)
    fun getOrNull(): ResultT? {
        return internalTask.getOrNull()
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
            config: TaskConfig? = TaskConfig(),
            block: suspend CoroutineScope.() -> ResultT
        ): Task<ResultT> {
            return Task(
                scope ?: ContextScope.getSupervisor(
                    DispatchersProvider.Default
                ), config, block
            )
        }
    }

    companion object {
        /**
         * Build that will create a task and it's logic
         * @param block That task logic
         */
        inline fun <ResultT> execute(
            retry: Int = 0,
            backOffDelay: Int = 0,
            startDelay: Int = 0,
            config: TaskConfig? = TaskConfig(retry, backOffDelay, startDelay),
            noinline block: suspend CoroutineScope.() -> ResultT
        ): Task<ResultT> {
            return Builder().withScope().execute(config, block)
        }

        @JvmStatic
        @JvmOverloads
        fun config(
            retry: Int = 0,
            backOffDelay: Int = 0,
            startDelay: Int = 0
        ): TaskConfig {
            return TaskConfig(
                retry = retry,
                backOffDelay = backOffDelay,
                startDelay = startDelay,
            )
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
    return runCatching { job.await() }.getOrElse { throw it.toClientException() }
}


/**
 * Wait for the task to finish
 * @return [ResultT] if succeeded or null if failed
 */
suspend fun <ResultT> Task<ResultT>.awaitOrNull(): ResultT? {
    return try {
        await()
    } catch (t: Throwable) {
        ClientException.listener.invoke(t)
        null
    }
}

@Builder
// @JsExport
data class TaskConfig(
    /**
     * If set will try to rerun the task if it fails with the set amount ,
     * Example
     * retry = 0 , run the task once
     * retry = 1, run the job first time if failed run again
     * retry = 2, run the job first time if failed run again, if failed run one more time
     */
    val retry: Int? = 0,
    /**
     * If set will add a delay of milli seconds when trying to rerun task after failure if [retry] is set
     */
    val backOffDelay: Int? = 0,
    /**
     * If set will delay starting the task with the set milli seconds
     */
    val startDelay: Int? = 0
) {
    companion object {

        @JvmStatic
        fun builder(): TaskConfigBuilder {
            return TaskConfigBuilder()
        }
    }
}