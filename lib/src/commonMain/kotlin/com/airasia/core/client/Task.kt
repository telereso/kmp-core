package com.airasia.core.client


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 *A discriminated union that encapsulates a successful outcome with a value of type [ResultT] or a failure with an arbitrary Throwable [ClientException].
 * @param scope aCoroutineScope defaulted to Default can provide your own scope as well, ensure its testable by injectin the provider.
 */
@ExperimentalJsExport
@JsExport
class Task<ResultT>(val scope: CoroutineScope = ContextScope.get(DispatchersProvider.Default)) {

    /**
     * This scope is used only created and used when invoking [_successUI] and [_failureUI]
     */
    private val _scopeUI: CoroutineScope by lazy {
        ContextScope.get(DispatchersProvider.Main)
    }
    /**
     * using the Task's instance, we can call success.invoke and passing a success result
     */
    var success: ((ResultT) -> Unit)? = null
    private var _successUI: ((ResultT) -> Unit)? = null

    /**
     * using the Task's instance, we can call failure.invoke passing in a preferred Throwable [ClientException] as a failure result
     */
    var failure: ((ClientException) -> Unit)? = null
    private var _failureUI: ((ClientException) -> Unit)? = null

    /**
     * using the Task's instance, we can cancel and running coroutines of this Task's scope.
     * alternaively we can use the Task's cancel fun @see [Task.cancel]
     */
    var cancelTask: ((ClientException) -> Unit)? = null

    /**
     * a success scope that is attachable to a task instance
     *      *  task().onSuccess {
     *     // do background logic , call another api
     *  }
     *  use this scope to  retrieve the success response of a task
     */
    fun onSuccess(action: (ResultT) -> Unit): Task<ResultT> {

        // Ignore duplicate success & successUI calls and accept the first pair only
        if (success != null && _successUI != null) return this

        success = { res ->
            action(res)
            _successUI?.let {
                _scopeUI.launch(DispatchersProvider.Main) {
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
        if (failure != null && _failureUI != null) return this

        failure = { e ->
            action(e)
            _failureUI?.let {
                _scopeUI.launch(DispatchersProvider.Main) {
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
    inline fun onCancel(noinline action: (ClientException) -> Unit): Task<ResultT> {
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
        if (success != null && _successUI != null) return this

        if (success != null) {
            _successUI = action
        } else {
            success = { res ->
                _scopeUI.launch(DispatchersProvider.Main) {
                    action.invoke(res)
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
        if (failure != null && _failureUI != null) return this

        if (failure != null) {
            _failureUI = action
        } else {
            failure = { e ->
                _scopeUI.launch(DispatchersProvider.Main) {
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

}