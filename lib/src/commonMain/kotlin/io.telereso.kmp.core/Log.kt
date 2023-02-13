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

import io.github.aakira.napier.Napier


object Log {

    fun Any?.logDebug(message: String, throwable: Throwable? = null) {
        Log.d(this, message, throwable)
    }

    fun Any?.logInfo(message: String, throwable: Throwable? = null) {
        Log.i(this, message, throwable)
    }

    fun Any?.logError(throwable: Throwable? = null, message: String? = "") {
        Log.e(this, throwable, message ?: throwable?.message ?: "")
    }

    fun d(caller: Any?, message: String, throwable: Throwable? = null) {
        Napier.d(message, throwable, caller?.let { it::class.simpleName })
    }

    fun i(caller: Any?, message: String, throwable: Throwable? = null) {
        Napier.i(message, throwable, caller?.let { it::class.simpleName })
    }

    fun e(caller: Any?, throwable: Throwable? = null, message: String? = "") {
        Napier.e(
            message ?: throwable?.message ?: "",
            throwable,
            caller?.let { it::class.simpleName })
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Napier.d(message, throwable, tag)
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Napier.i(message, throwable, tag)
    }

    fun e(tag: String, throwable: Throwable? = null, message: String? = "") {
        Napier.e(message ?: throwable?.message ?: "", throwable, tag)
    }
}


