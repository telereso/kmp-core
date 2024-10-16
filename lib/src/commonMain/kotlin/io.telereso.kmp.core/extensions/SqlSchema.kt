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

package io.telereso.kmp.core.extensions

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import io.telereso.kmp.core.Log

/**
 * Performs a destructive migration on the provided SQL driver.
 *
 * This method drops all tables from the database, excluding system tables like
 * `sqlite_sequence` and `android_metadata`. After all tables are dropped,
 * the schema is recreated using the `create` method.
 *
 * @receiver The `SqlSchema` that defines the structure of the database.
 * @param driver The `SqlDriver` instance that will be used to execute the SQL commands.
 * @return A `QueryResult.AsyncValue<Unit>` representing the result of the migration process.
 *         The schema is recreated after all tables are dropped.
 *
 * ### Example:
 * ```kotlin
 * object DestructiveMigrationSchema :
 *     SqlSchema<QueryResult.AsyncValue<Unit>> by MyClientDatabase.Schema {
 *     override fun migrate(
 *         driver: SqlDriver, oldVersion: Long, newVersion: Long, vararg callbacks: AfterVersion
 *     ): QueryResult.AsyncValue<Unit> {
 *         Log.d(
 *             MyClientManager.TAG,
 *             "Database version changed ($oldVersion -> $newVersion), performing destructive migration"
 *         )
 *         return destructiveMigration(driver)
 *     }
 * }
 *
 * fun MynClientDatabase.Companion.DestructiveMigrationSchema():
 *         SqlSchema<QueryResult.AsyncValue<Unit>> = DestructiveMigrationSchema
 * ```
 * In this example, the `DestructiveMigrationSchema` object implements the `SqlSchema` interface.
 * When the `migrate` method detects a version change, it logs the change and invokes
 * `destructiveMigration(driver)` to drop and recreate all tables.
 *
 */
fun SqlSchema<QueryResult.AsyncValue<Unit>>.destructiveMigration(driver: SqlDriver): QueryResult.AsyncValue<Unit> {
    val tables = driver.getTables()

    for (table in tables) {
        driver.execute(identifier = null, sql = "DROP TABLE $table", parameters = 0)
    }

    return create(driver)
}

fun SqlSchema<QueryResult.AsyncValue<Unit>>.destructiveMigration() =
    object : SqlSchema<QueryResult.AsyncValue<Unit>> {
        override val version = this@destructiveMigration.version

        override fun create(driver: SqlDriver): QueryResult.AsyncValue<Unit> =
            this@destructiveMigration.create(driver)

        override fun migrate(
            driver: SqlDriver,
            oldVersion: Long,
            newVersion: Long,
            vararg callbacks: AfterVersion,
        ) = run {
            Log.i(
                "SqlDriverFactory",
                "Database version changed ($oldVersion -> $newVersion), performing destructive migration"
            )
            destructiveMigration(driver)
        }
    }