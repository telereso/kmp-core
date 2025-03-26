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

import CoreJs
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.WebWorkerDriver
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
import org.w3c.dom.Worker

internal var isReactNativePlatform = false
internal var isWeChatPlatform = false

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
) = HttpClient(if (isReactNativePlatform || isWeChatPlatform) CoreJs else Js) {

    config(this)

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
@JsExport
fun debugLogger() {
    debugLoggerInternal()
}

actual open class SqlDriverFactory actual constructor(
    actual val databaseName: String,
    actual val asyncSchema: SqlSchema<QueryResult.AsyncValue<Unit>>
) {
    actual open fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? = null
    actual open suspend fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker(
                js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
            )
        ).also { asyncSchema.create(it).await() }
    }
}