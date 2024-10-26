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

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext
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
import io.telereso.kmp.core.extensions.destructiveMigration
import io.telereso.kmp.core.extensions.getTables
import io.telereso.kmp.core.extensions.sync
import io.telereso.kmp.core.models.ClientException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import platform.Foundation.NSLocalizedFailureReasonErrorKey
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURLErrorFailingURLErrorKey
import platform.UIKit.UIDevice

/**
 * Since the rest of the Darwin targets depend on iOSMain during its creation,
 * All the actual implementations can be reuse and pickup from iOsMain
 * On in special occasions when we need custom implementation on like MacOS or WatchOS we
 * can use the specific target module.
 */
class IOSPlatform : Platform() {
    override val type: Type = Type.IOS

    /**
     * example : MyApp/1 iPhone5,2 iOS/10_1 CFNetwork/808.3 Darwin/16.3.0
     */
    override val userAgent: String =
        "${UIDevice.currentDevice.systemName()}/${UIDevice.currentDevice.systemVersion} ${UIDevice.currentDevice.name}"


}

fun Platform.isSimulator(): Boolean {
    return NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
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

    if (shouldLogHttpRequests) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
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

/**
 * Converts a [ClientException] into an [NSError] object for better interop with Objective-C, Swift projects.
 * @return The [NSError] object created from the [ClientException].
 */
@OptIn(ExperimentalForeignApi::class)
fun ClientException.toNSError(): NSError {
    // Create a mutable map to hold the error information.
    val userInfo = mutableMapOf<String?, Any>()

    // Add the URL that caused the error (if available).
    httpURl?.let { userInfo[NSURLErrorFailingURLErrorKey] = it }

    // Add the cause of the error (if available).
    cause?.let { userInfo["cause"] = it }

    // Add a localized description of the error.
    userInfo[NSLocalizedDescriptionKey] = "Something went wrong!"

    // Add a localized explanation of the reason for the error (if available).
    message?.let { userInfo[NSLocalizedFailureReasonErrorKey] = it }

    // Add the HTTP status code (if available).
    httpStatusCode?.let { userInfo["httpStatusCode"] = it }

    // Add the error body (if available).
    errorBody?.let { userInfo["errorBody"] = it }

    code?.let { userInfo["code"] = it }

    // Add the error type (if available).
    errorType?.let { userInfo["errorType"] = it }

    // Add the error string (if available).
    errorString?.let { userInfo["errorString"] = it }

    // Create a new map from the existing userInfo map.
    val userInfoMap: Map<Any?, *> = userInfo.toMap()

    // Create and return the NSError object with the appropriate information.
    return NSError(domain = this::class.simpleName, code = (httpStatusCode ?: 0).convert(), userInfo = userInfoMap)
}

actual open class SqlDriverFactory actual constructor(
    actual val databaseName: String,
    actual val asyncSchema: SqlSchema<QueryResult.AsyncValue<Unit>>
) {
    private val mutex = Mutex()
    actual open fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? = asyncSchema.sync()
    actual open suspend fun createDriver(): SqlDriver {
        val schema = getSchema()!!
        var sqlDriver = NativeSqliteDriver(schema, databaseName)
        return mutex.withLock {
            runCatching {
                sqlDriver.getTables()
                sqlDriver
            }.getOrElse {
                Log.i(
                    "SqlDriverFactory",
                    "Failed to create NativeSqliteDriver for $databaseName!, deleting database files and trying again"
                )
                DatabaseFileContext.deleteDatabase(databaseName)
                sqlDriver = NativeSqliteDriver(schema, databaseName)
                Log.i("SqlDriverFactory", "Database ($databaseName) after clean, Tables: ${sqlDriver.getTables()}")
                sqlDriver
            }
        }
    }
}

fun SqlSchema<QueryResult.AsyncValue<Unit>>.destructiveMigrationSynchronous() =
    object : SqlSchema<QueryResult.Value<Unit>> {
        override val version = this@destructiveMigrationSynchronous.version


        override fun create(driver: SqlDriver) = QueryResult.Value(
            runBlocking {
                this@destructiveMigrationSynchronous.create(driver).await()
            },
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