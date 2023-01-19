package com.airasia.core.client

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
        ANDROID, IOS, JS, JVM
    }

    /**
     * type of the platforms based on the [TYPE] enum.
     */
    val type: TYPE

    /**
     * name of the platform. mostly passed as lower case of the [TYPE] enum.
     */
    val name: String
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