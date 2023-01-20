package io.telereso.core.client

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

/**
 * A class we define platform specific  or actual implmentations for JS Target
 */

class JSPlatform : Platform {
    override val type: Platform.TYPE = Platform.TYPE.JS
    override val name: String = type.name.lowercase()
}

/**
 * expected Platform imlmentation for JS platform
 * @return the platform .
 */
actual fun getPlatform(): Platform = JSPlatform()

val jsonClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json(Http.ktorConfigJson)
    }
}

actual fun httpClient(
    shouldLogHttpRequests: Boolean ,
    interceptors: List<Any?>?,
    userAgent: String?,
    config: HttpClientConfig<*>.() -> Unit) = jsonClient

/**
 * Called from the client to initialize Napier logger
 */
@JsExport
fun initLogger() {
    Napier.base(DebugAntilog("CoreClient"))
}