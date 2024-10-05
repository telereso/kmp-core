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

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class JvmPlatform : Platform() {
    override val type: Type = Type.JVM
    override val userAgent: String = System.getProperty("http.agent","kmpJvmPlatform")
}

/**
 * expected Platform implementation for JVM platform
 * @return the platform .
 */
actual fun getPlatform(): Platform = JvmPlatform()

actual fun httpClient(
    shouldLogHttpRequests: Boolean,
    interceptors: List<Any?>?,
    userAgent: String?,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient(OkHttp) {

    // https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor
    val logging = HttpLoggingInterceptor().also {
        it.setLevel(HttpLoggingInterceptor.Level.BODY)
    }
    config(this)

    install(UserAgent) {
        agent = userAgent ?: getPlatform().userAgent
    }

    /**
     * needed to define the transformation of the HttClient.
     * JsonFeature was replaced by ContentNegotiation in Ktor 2 above
     * https://ktor.io/docs/serialization-client.html#add_content_negotiation_dependency
     * Ktor has a set of plugins already built in that are disabled by default. The
    ContentNegotiation, for example, allows you to deserialize responses,
     */
    /**
     * needed to define the transformation of the HttClient.
     * JsonFeature was replaced by ContentNegotiation in Ktor 2 above
     * https://ktor.io/docs/serialization-client.html#add_content_negotiation_dependency
     * Ktor has a set of plugins already built in that are disabled by default. The
    ContentNegotiation, for example, allows you to deserialize responses,
     */
    install(ContentNegotiation) {
        /**
         * Since it’s JSON, you can install
        ContentNegotiation for json so the response from this function will be the
        deserialized object.
        Also contentnegotion is needed to pass data objects as body in a request.
         */
        /**
         * Since it’s JSON, you can install
        ContentNegotiation for json so the response from this function will be the
        deserialized object.
        Also contentnegotion is needed to pass data objects as body in a request.
         */
        json(Http.ktorConfigJson)
    }

    engine {
        // Here we can add usual network interceptors.
        interceptors?.forEach {
            if (it is Interceptor)
                addInterceptor(it)
        }
        if (shouldLogHttpRequests) {
            addInterceptor(logging)
        }
        config {
            retryOnConnectionFailure(true)
            connectTimeout(0, TimeUnit.SECONDS)
        }
    }
}

actual abstract class SqlDriverFactory actual constructor(actual val databaseName: String) {
    actual abstract fun getAsyncSchema(): SqlSchema<QueryResult.AsyncValue<Unit>>
    actual open fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? = null
    actual open suspend fun createDriver(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { getAsyncSchema().create(it).await() }
}