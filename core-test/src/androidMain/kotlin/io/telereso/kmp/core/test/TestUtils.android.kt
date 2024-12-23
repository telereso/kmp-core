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

package io.telereso.kmp.core.test

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.telereso.kmp.core.SqlDriverFactory
import java.io.File


actual class Resource actual constructor(actual val name: String) {
    private val file = File("${TestUtils.RESOURCE_PATH}/$name")

    actual suspend fun exists(): Boolean = file.exists()

    actual suspend fun readText(): String = file.readText()

    actual suspend fun writeText(text: String) = file.writeText(text)
}

/**
 * A wrapper class of [SqlDriverFactory]
 * It will make sure to use InMemory databases for the platforms that are persistence such as android and ios
 * To avoid test cases from conflicting
 * Also creates a new database for each use case by overriding the db name everytime a new factory is created
 *
 * @constructor Creates an instance of `TestSqlDriverFactory` with the provided SQL driver factory and
 * optional database name override.
 *
 * @param sqlDriverFactory main driver factory to be wrapper with in memory test drivers
 * @param overrideName if true it will add prefix to the database name to make sure every new driver is using a unique database to avoid conflicts, eg dbName: my-client.db will be 1-my-client.db, 2-my-client.db
 */
actual class TestSqlDriverFactory actual constructor(
    val sqlDriverFactory: SqlDriverFactory,
    overrideName: Boolean
) : SqlDriverFactory(sqlDriverFactory.databaseName(overrideName), sqlDriverFactory.asyncSchema) {
    actual override fun getSchema() = sqlDriverFactory.getSchema()
    actual override suspend fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            .also { sqlDriverFactory.asyncSchema.create(it).await() }
    }
}