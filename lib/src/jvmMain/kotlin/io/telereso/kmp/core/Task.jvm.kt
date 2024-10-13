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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture


internal actual class InternalTask<ResultT> actual constructor(_task: Task<ResultT>) {
    internal actual val task: Task<ResultT> = _task

    fun future(): CompletableFuture<ResultT> {
        return GlobalScope.future { task.await() }
    }

    actual fun get(): ResultT {
        return future().get()
    }

    actual fun getOrNull(): ResultT? {
        return runCatching { future().get() }.getOrNull()
    }
}

object Tasks {
    @JvmStatic
    fun <ResultT> Task<ResultT>.future(): CompletableFuture<ResultT> {
        return GlobalScope.future { await() }
    }

    @JvmStatic
    @JvmOverloads
    fun <ResultT> create(
        config: TaskConfig? = TaskConfig(),
        callable: Callable<ResultT>
    ): Task<ResultT> {
        return Task.execute(config = config) {
            callable.call()
        }
    }
}