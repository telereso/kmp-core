
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


import app.cash.sqldelight.async.coroutines.awaitAsList
import io.telereso.kmp.core.SqlDriverFactory
import io.telereso.kmp.core.Task
import io.telereso.kmp.core.extensions.destructiveMigration
import io.telereso.kmp.core.test.TestSqlDriverFactory
import io.telereso.kmp.core.test.cache.CoreClientTestDatabase

@JsExport
object TestDataBase {
    private var db: CoreClientTestDatabase? = null
    fun initDb(): Task<Boolean> {
        return Task.execute {
            val factory = TestSqlDriverFactory(
                SqlDriverFactory("test.db", CoreClientTestDatabase.Schema.destructiveMigration()),
                overrideName = false
            )
            db = CoreClientTestDatabase(factory.createDriver())
            true
        }
    }

    fun addUser(name: String): Task<Boolean> {
        return Task.execute {
            db?.coreClientTestDatabaseQueries?.insertUser(name)
            true
        }
    }

    fun getUsers(): Task<Array<String>> {
        return Task.execute {
            db?.coreClientTestDatabaseQueries
                ?.selectAll()?.awaitAsList()?.toList()
                ?.map { it.name }
                ?.toTypedArray()
                ?: emptyArray()
        }
    }
}

