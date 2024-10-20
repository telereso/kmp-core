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

import app.cash.sqldelight.async.coroutines.awaitAsList
import io.kotest.matchers.shouldBe
import io.telereso.kmp.core.SqlDriverFactory
import io.telereso.kmp.core.extensions.destructiveMigration
import io.telereso.kmp.core.extensions.getTables
import io.telereso.kmp.core.extensions.sync
import io.telereso.kmp.core.test.cache.CoreClientTestDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SqlDriverFactoryTest {

    @Test
    fun testCreate() = runTest {
        val factory =
            TestSqlDriverFactory(SqlDriverFactory("test.db", CoreClientTestDatabase.Schema))
        val db = CoreClientTestDatabase(factory.createDriver())
        db.coreClientTestDatabaseQueries.insertUser("test1")
        db.coreClientTestDatabaseQueries.selectAll().awaitAsList()[0].name.shouldBe("test1")
    }

    @Test
    fun testDestructiveMigration() = runTest {
        val factory =
            TestSqlDriverFactory(SqlDriverFactory("test.db", CoreClientTestDatabase.Schema.destructiveMigration()))
        val db = CoreClientTestDatabase(factory.createDriver())
        db.coreClientTestDatabaseQueries.insertUser("test1")
        db.coreClientTestDatabaseQueries.selectAll().awaitAsList()[0].name.shouldBe("test1")
    }

    @Test
    fun testGetTables() = runTest {
        val factory =
            TestSqlDriverFactory(SqlDriverFactory("test.db", CoreClientTestDatabase.Schema.destructiveMigration()))
        factory.createDriver().getTables()[0].shouldBe("users")
    }
}