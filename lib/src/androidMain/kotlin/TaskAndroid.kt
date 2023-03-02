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
import kotlinx.coroutines.CoroutineScope


actual class Task<ResultT> private actual constructor(
    scope: CoroutineScope,
    block: suspend CoroutineScope.() -> ResultT
) {

    actual val task = InternalTask.builder().withScope(scope).execute(block)

    @JvmOverloads
    fun cancel(message: String, throwable: ClientException? = null) {
        task.cancel(message, throwable)
    }

    suspend fun await(): ResultT {
        return task.await()
    }

    suspend fun awaitOrNull(): ResultT? {
        return task.awaitOrNull()
    }

    fun onSuccess(action: (ResultT) -> Unit): Task<ResultT> {
        task.onSuccess(action)
        return this
    }

    fun onSuccessUI(action: (ResultT) -> Unit): Task<ResultT> {
        task.onSuccessUI(action)
        return this
    }

    fun onFailure(action: (ClientException) -> Unit): Task<ResultT> {
        task.onFailure(action)
        return this

    }

    fun onFailureUI(action: (ClientException) -> Unit): Task<ResultT> {
        task.onFailureUI(action)
        return this
    }

    fun onCancel(action: (ClientException) -> Unit): Task<ResultT> {
        task.onCancel(action)
        return this
    }

    actual class Builder {
        actual var scope: CoroutineScope? = null

        /**
         * provide your own scope for the task to run on
         */
        actual fun withScope(scope: CoroutineScope): Builder {
            this.scope = scope
            return this
        }

        /**
         * @param block provide your logic
         */
        actual fun <ResultT> execute(block: suspend CoroutineScope.() -> ResultT): Task<ResultT> {
            return Task(scope ?: ContextScope.get(DispatchersProvider.Default), block)
        }

    }

    actual companion object {
        /**
         * Build that will create a task and it's logic
         * @param block That task logic
         */
        actual inline fun <ResultT> execute(noinline block: suspend CoroutineScope.() -> ResultT): Task<ResultT> {
            return Builder().withScope().execute(block)
        }

        @JvmStatic
        internal actual fun builder(): Builder {
            return Builder()
        }

    }
}