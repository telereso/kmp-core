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

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.telereso.kmp.core.models.ClientException
import kotlinx.browser.window

// @JsExport
actual class CoreClient {
    actual companion object {
        private val instant: CoreClient by lazy {
            CoreClient()
        }

        /**
         * Called from the client to initialize Napier logger
         */
        actual fun debugLogger() {
            debugLoggerInternal()
        }

        fun get(): CoreClient {
            return instant
        }
    }

    fun debugLogger() {
        CoreClient.debugLogger()
    }

    actual fun isAppInstalled(
        packageName: String,
        fingerprint: String?,
        alg: String
    ): Boolean {
        throw ClientException("Android/iOS only functionally ")
    }

    /**
     * This is to be used by a sdk only,
     * Verify that the current application is allowed to use the calling library
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    actual fun verifyConsumer(allowed: List<Consumer>) {
        if (!allowed.any {
                it.platform == Platform.Type.BROWSER && it.appId.replace("*", ".*").toRegex()
                    .matches(window.location.host)
            }) {
            throw ClientException("Website (${window.location.host}) not allowed to use this sdk")
        }
    }

    /**
     * This is to be used by a sdk only,
     * Verify that the current application is allowed to use the calling library
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    @JsName("verifyCurrentConsumer")
    actual fun verifyConsumer(vararg allowed: Consumer) {
        verifyConsumer(allowed.toList())
    }
}

/**
 * Return Singleton CoreClient
 */
// @JsExport
fun get(): CoreClient {
    return CoreClient.get()
}