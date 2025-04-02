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

import io.telereso.kmp.annotations.JsOnlyExport
import kotlin.js.JsExport
import kotlin.jvm.JvmOverloads

expect class CoreClient {
    companion object {
        fun debugLogger()
    }

    /**
     * Use this function to check if an application is installed on the user's device
     * Platform : Android, iOS
     * Will through exceptions for JS and JVM
     * @param packageName android application package name or ios app scheme
     * @param fingerprint the singing fingerprint for android only
     * @param alg [fingerprint] algorithm for android only
     */
    @JvmOverloads
    fun isAppInstalled(
        packageName: String, fingerprint: String? = null, alg: String = "SHA-256" // or SHA1
    ): Boolean

    /**
     * verify that the current application or website is allowed to use the called library
     * Platforms: Android, iOS, JS (browser)
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    fun verifyConsumer(allowed: List<Consumer>)

    /**
     * verify that the current application or website is allowed to use the called library
     * Platforms: Android, iOS, JS (browser)
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    fun verifyConsumer(vararg allowed: Consumer)
}

@JsOnlyExport
data class Consumer(
    val platform: Platform.Type,
    /**
     * Android: provide app package id, eg: com.example.myapp
     * iOS: provide app bundle id eg: com.example.myapp
     * Web: provide origin domain, eg: myapp.example.com
     */
    val appId: String,
    /**
     * This will be android's singing fingerprint
     */
    val fingerprint: String? = null,
    /**
     * Use with [fingerprint], can be "SHA-256" or "SHA-1"
     */
    val alg: String = DEFAULT_ALG
) {

    companion object {
        const val DEFAULT_ALG = "SHA-256"

        /**
         * Create an allowed android application [Consumer] to use the sdk
         */
        fun android(applicationId: String, fingerprint: String, alg: String = DEFAULT_ALG): Consumer {
            return Consumer(Platform.Type.ANDROID, applicationId, fingerprint, alg)
        }

        /**
         * Create an allowed ios application [Consumer] to use the sdk
         */
        fun ios(bundleId: String): Consumer {
            return Consumer(Platform.Type.IOS, bundleId)
        }

        /**
         * Create an allowed website application [Consumer] to use the sdk
         */
        fun website(host: String): Consumer {
            return Consumer(Platform.Type.BROWSER, host)
        }
    }
}