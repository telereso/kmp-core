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
fun initLogger() {
    Napier.base(DebugAntilog())
}