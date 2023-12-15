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

actual class CoreClient {
    actual companion object {
        actual fun debugLogger() {
        }
    }

    /**
     * Use this function to check if an application is installed on the user's device
     * Platform : Android, iOS
     * Will through exceptions for JS and JVM
     * @param packageName android application package name or ios app scheme
     * @param fingerprint the singing fingerprint for android only
     * @param alg [fingerprint] algorithm for android only
     */
    actual fun isAppInstalled(
        packageName: String,
        fingerprint: String?,
        alg: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * verify that the current application or website is allowed to use the called library
     * Platforms: Android, iOS, JS (browser)
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    actual fun verifyConsumer(allowed: List<Consumer>) {
    }

    /**
     * verify that the current application or website is allowed to use the called library
     * Platforms: Android, iOS, JS (browser)
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    actual fun verifyConsumer(vararg allowed: Consumer) {
    }

}