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
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.window

/**
 * A class we define platform specific  or actual implementations for JS Target
 */

class BrowserPlatform : Platform() {
    override val type: Type = Type.BROWSER
    override val userAgent: String =
        runCatching { window.navigator.userAgent }.getOrNull() ?: "kmpBrowserPlatform"
}

class NodeJsPlatform : Platform() {
    override val type: Type = Type.NODE
    override val userAgent: String = "kmpNodeJSPlatform"
}

/**
 * expected Platform implementation for JS platform
 * @return the platform .
 */
actual fun getPlatform(): Platform =
    if (runCatching { window.navigator }.getOrNull() != null) BrowserPlatform() else NodeJsPlatform()

actual fun httpClient(
    shouldLogHttpRequests: Boolean,
    interceptors: List<Any?>?,
    userAgent: String?,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient(Js) {

    install(UserAgent) {
        agent = userAgent ?: getPlatform().userAgent
    }

    install(ContentNegotiation) {
        json(Http.ktorConfigJson)
    }

    if (shouldLogHttpRequests) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }
}


internal fun debugLoggerInternal(){
    Napier.base(DebugAntilog("CoreClient"))
}

/**
 * Called from the client to initialize Napier logger
 */
//@JsExport
fun debugLogger() {
    debugLoggerInternal()
}