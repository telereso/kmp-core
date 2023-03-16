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
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import platform.UIKit.UIDevice


/**
 * Since the rest of the Darwin targets depend on iOSMain during its creation,
 * All the actual implementations can be reuse and pickup from iOsMain
 * On in special occasions when we need custom implementation on like MacOS or WatchOS we
 * can use the specific target module.
 */
class IOSPlatform : Platform {
    override val type: Platform.TYPE = Platform.TYPE.IOS

    /**
     * example : MyApp/1 iPhone5,2 iOS/10_1 CFNetwork/808.3 Darwin/16.3.0
     */
    override val userAgent: String = "${UIDevice.currentDevice.systemName()}/${UIDevice.currentDevice.systemVersion} ${UIDevice.currentDevice.name}"
}

/**
 * expected Platform imlmentation for iOS platform
 * @return the platform .
 */
actual fun getPlatform(): Platform = IOSPlatform()

/**
 * actual declaration of the httpClient function for the iOS
 */
actual fun httpClient(
    shouldLogHttpRequests: Boolean,
    interceptors: List<Any?>?,
    userAgent: String?,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient(Darwin) {
    config(this)

    if(shouldLogHttpRequests) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.HEADERS
        }
    }

    install(UserAgent) {
        agent = userAgent ?: getPlatform().userAgent
    }

    install(ContentNegotiation) {
        json(Http.ktorConfigJson)
    }

    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}

/**
 * Called from the client to initialize Napier logger
 */
@Deprecated(
    "Due to ios naming conflict moving this to an object",
    ReplaceWith("CoreClient.debugLogger()")
)
fun initLogger() {
    Napier.base(DebugAntilog())
}

object CoreClient {
    /**
     * Called from the client to initialize Napier logger
     */
    fun debugLogger() {
        Napier.base(DebugAntilog())
    }
}