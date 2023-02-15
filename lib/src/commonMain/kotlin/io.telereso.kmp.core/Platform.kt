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

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Defines the supported platforms by the sdks
 */
interface Platform {

    /**
     * @suppress
     */
    enum class TYPE {
        ANDROID, IOS, BROWSER, JVM
    }

    /**
     * type of the platforms based on the [TYPE] enum.
     */
    val type: TYPE

    /**
     * User agent for http requests
     */
    val userAgent: String
}

/**
 * expects inplmentation on all supported platforms
 * @return the platform .
 */
expect fun getPlatform(): Platform

/**
 * To  configure Ktor engine-specific options in our multiplatform SDK we declare expect/actual dependecies
 * Notice by default we set it to Unit
 * we can also define this expect within the network package so not accessable to client.
 * We have to write some actual and expected functions that will resolve all platform’s conflicts in iOS and Android and JS.
 * It calls different HTTP functions in different platforms which you can define platform-specific.
 *
 */
expect fun httpClient(
    shouldLogHttpRequests: Boolean = false,
    interceptors: List<Any?>? = listOf(),
    userAgent: String? = null,
    config: HttpClientConfig<*>.() -> Unit = {}
): HttpClient