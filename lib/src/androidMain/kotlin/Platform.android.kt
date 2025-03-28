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

import android.content.Context
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.telereso.kmp.core.extensions.destructiveMigration
import io.telereso.kmp.core.extensions.sync
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * A AndroidPlatform class that defines the Android platform type and name.
 * References the names from the Android platform specific [Build] class
 */
class AndroidPlatform : Platform() {
    override val type: Type = Type.ANDROID

    /**
     * example <AppName>/<version> Dalvik/<version> (Linux; U; Android <android version>; <device ID> Build/<buildtag>)
     */
    override val userAgent: String = System.getProperty("http.agent")
        ?: "(Linux; U; Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"
}

actual fun getPlatform(): Platform = AndroidPlatform()

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
        interceptors?.iterator()?.forEach {
            if (it is Interceptor)
                addInterceptor(it)
        }
        // Here we can add out usual network interceptors.
        if (shouldLogHttpRequests) {
            // Here we can add out usual network interceptors.
            addInterceptor(logging)
        }
        //addInterceptor(ChuckerInterceptor(context))
        config {
            retryOnConnectionFailure(true)
            connectTimeout(0, TimeUnit.SECONDS)
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

actual open class SqlDriverFactory actual constructor(
    actual val databaseName: String,
    actual val asyncSchema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ) {
    private var context: Context? = null
    private var sqlDriver: SqlDriver? = null

    constructor(
        context: Context?,
        databaseName: String,
        asyncSchema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ) : this(databaseName, asyncSchema) {
        this.context = context
    }

    private val mutex = Mutex()
    actual open fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? = asyncSchema.sync()
    actual open suspend fun createDriver(): SqlDriver {
        val schema = getSchema()!!
        return mutex.withLock {
            AndroidSqliteDriver(schema, context!!, databaseName,
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onDowngrade(
                    db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int
                ) {
                    sqlDriver?.let {
                        schema.migrate(
                            AndroidSqliteDriver(db, cacheSize = 1),
                            oldVersion.toLong(),
                            newVersion.toLong()
                        )
                    }
                }
            }).apply { sqlDriver = this }
        }
    }
}

fun SqlSchema<QueryResult.AsyncValue<Unit>>.destructiveMigrationSynchronous() =
    object : SqlSchema<QueryResult.Value<Unit>> {
        override val version = this@destructiveMigrationSynchronous.version

        override fun create(driver: SqlDriver) = QueryResult.Value(
            runBlocking { this@destructiveMigrationSynchronous.create(driver).await() },
        )

        override fun migrate(
            driver: SqlDriver,
            oldVersion: Long,
            newVersion: Long,
            vararg callbacks: AfterVersion,
        ) = QueryResult.Value(
            runBlocking {
                Log.i(
                    "SqlDriverFactory",
                    "Database version changed ($oldVersion -> $newVersion), performing destructive migration"
                )
                destructiveMigration(driver).await()
            },
        )
    }


