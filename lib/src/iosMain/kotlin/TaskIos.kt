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
import kotlinx.coroutines.runBlocking


actual class Task<ResultT> private actual constructor(
    scope: CoroutineScope,
    block: suspend CoroutineScope.() -> ResultT
) {

    actual val _internalTask = InternalTask.builder().withScope(scope).execute(block)

    actual fun cancel(message: String, throwable: ClientException?) {
        _internalTask.cancel(message, throwable)
    }

    actual fun cancel(message: String) {
        _internalTask.cancel(message)
    }

    fun get(): ResultT {
        return runBlocking { _internalTask.get() }
    }

    suspend fun getOrNull(): ResultT? {
        return runBlocking { _internalTask.getOrNull() }
    }

    actual fun onSuccess(action: (ResultT) -> Unit): Task<ResultT> {
        _internalTask.onSuccess(action)
        return this
    }

    actual fun onSuccessUI(action: (ResultT) -> Unit): Task<ResultT> {
        _internalTask.onSuccessUI(action)
        return this
    }

    actual fun onFailure(action: (ClientException) -> Unit): Task<ResultT> {
        _internalTask.onFailure(action)
        return this

    }

    actual fun onFailureUI(action: (ClientException) -> Unit): Task<ResultT> {
        _internalTask.onFailureUI(action)
        return this
    }

    actual fun onCancel(action: (ClientException) -> Unit): Task<ResultT> {
        _internalTask.onCancel(action)
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
            return Builder().execute(block)
        }

        actual fun builder(): Builder {
            return Builder()
        }

    }
}