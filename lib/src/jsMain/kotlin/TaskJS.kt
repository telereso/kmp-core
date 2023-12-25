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
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise


internal actual class InternalTask<ResultT> actual constructor(_task: Task<ResultT>) {
    internal actual val task: Task<ResultT> = _task

    actual fun get(): ResultT {
        throw ClientException("Use async instead for blocking calls")
    }

    actual fun getOrNull(): ResultT? {
        throw ClientException("Use async instead for blocking calls")
    }
}

// @JsExport
object Tasks {
    fun <ResultT> Task<ResultT>.async(): Promise<ResultT> {
        return Promise { success: (ResultT) -> Unit, failure: (Throwable) -> Unit ->
            onSuccess {
                success(it)
            }.onFailure {
                failure(it)
            }
        }
    }

    fun <ResultT> create(action: () -> ResultT): Task<ResultT> {
        return create(TaskConfig(),action)
    }

    @JsName("createWithConfig")
    fun <ResultT> create(config: TaskConfig,action: () -> ResultT): Task<ResultT> {
        return Task.execute(config = config) {
            action()
        }
    }

    fun <ResultT> from(action: Promise<ResultT>): Task<ResultT> {
        return Task.execute {
            action.asDeferred().await()
        }
    }

    fun fromString(action: Promise<String>): Task<String> {
        return Task.execute {
            action.asDeferred().await()
        }
    }

    fun fromNumber(action: Promise<Int>): Task<Int> {
        return Task.execute {
            action.asDeferred().await()
        }
    }

    fun fromBoolean(action: Promise<Boolean>): Task<Boolean> {
        return Task.execute {
            action.asDeferred().await()
        }
    }

    fun fromVoid(action: Promise<Unit>): Task<Unit> {
        return Task.execute {
            action.asDeferred().await()
        }
    }
}
