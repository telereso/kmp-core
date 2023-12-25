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

import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.URLProtocol
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * All common config for client mangers
 *
 * @param builder used to construct.
 * @constructor a private constructor due to the build pattern.
 * @author Caeser Bakabulindi
 * @since 11/11/2022
 */
@ExperimentalJsExport
//@JsExport
class Config private constructor(
    val builder: Builder
) {

    fun buildUpon(): Builder {
        return builder.copy()
    }

    /**
     * class companion class
     */
    companion object {
        /**
         * a Kotlin DSL fun that uses scope to build the Manager.
         * @param appName a mandatory value needed to be passed
         * @param appVersion a mandatory value needed to be passed
         */
        @JsName("dslBuilder")
        inline fun builder(
            appName: String,
            appVersion: String,
            block: Builder.() -> Unit = {}
        ) =
            Builder(appName, appVersion)
                .apply(block)
                .build()

        @JvmStatic
        fun builder(
            appName: String,
            appVersion: String,
        ): Builder {
            return Builder(appName, appVersion)
        }
    }

    /**
     * We may need to pass some compulsory values to the builder.
     * Mandatory param values are part of the constructor.
     * @param appName mandatory param for the current app name that is using the client
     * @param appVersion mandatory param for the current app version of client
     */
    data class Builder(val appName: String, val appVersion: String) {
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
