package io.telereso.core.client

import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.URLProtocol
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.native.concurrent.ThreadLocal

/**
 * This class acts as the point of entry into the client sdk.
 *
 * @param builder used to construct.
 * @constructor a private constructor due to the build pattern.
 * @author Caeser Bakabulindi
 * @since 11/11/2022
 */
@ExperimentalJsExport
@JsExport
class Config private constructor(
    val builder: Builder
) {

    /**
     * class companion class
     */
    @ThreadLocal
    companion object {
        /**
         * a Kotlin DSL fun that uses scope to build the Manager.
         * @param databaseDriverFactory a mandatory value needed to be passed
         */
        inline fun builder(
            appVersion: String,
            block: Builder.() -> Unit
        ) =
            Builder(appVersion)
                .apply(block)
                .build()
    }

    /**
     * We may need to pass some compulsory values to the builder.
     * Mandatory param values are part of the constructor.
     * @param appVersion mandatory param for the current app version of client
     */
    class Builder(val appVersion: String) {
        var interceptors: List<Any?>? = null
        var logHttpRequests: Boolean = false
        var environment: Environment? = null
        var requestTimeoutMillis: Long = 0L
        var connectTimeoutMillis: Long = 0L
        var host: String? = null
        var protocol: String? = null

        /**
         * all our optional values
         */
        fun withInterceptors(value: List<Any?>?): Builder {
            this.interceptors = value
            return this
        }

        /**
         * should log https requests
         */
        fun shouldLogHttpRequests(value: Boolean): Builder {
            this.logHttpRequests = value
            return this
        }

        /**
         * set the build enviroment
         * if not set, it will default to  [Environment.STAGING] as default
         */
        fun withEnvironment(value: Environment?): Builder {
            this.environment = value
            return this
        }

        /**
         * set [HttpTimeout] request Timeout in Millis
         * @param value of Int to support JS. later we can convert it to LONG
         * If not set, it will default to  [REQUEST_TIME_OUT_MILLIS] in constant as default
         */
        fun withRequestTimeoutInMillis(value: Int): Builder {
            this.requestTimeoutMillis = value.toLong()
            return this
        }

        /**
         * set [HttpTimeout] connection Timeout in Millis
         * If not set, it will default to  [CONNECTION_TIME_OUT_MILLIS] in constant as default
         */
        fun withConnectTimeoutInMillis(value: Int): Builder {
            this.connectTimeoutMillis = value.toLong()
            return this
        }

        /**
         * Ktor url builder must provide a protocol else it defaults to HTTP.
         * Client can only need to send us the url path.
         * @param host the url host without a /
         * @param protocol the url protocol. if empty it will default to HTTPS. all caps or small does not have any effect. if pass illegal protocol the API will fail.
         * check [URLProtocol] for supported protocols and proper namings.
         * [value] a consumer host path. e.g stg.example.com minus the http prefix and / postfix
         */
        fun withHost(protocol: String?, host: String?): Builder {
            this.host = host
            this.protocol = protocol
            return this
        }

        fun build(): Config {
            return Config(this)
        }
    }
}
