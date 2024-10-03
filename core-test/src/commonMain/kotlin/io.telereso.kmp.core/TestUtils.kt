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
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class TestUtils {

    companion object {
        const val RESOURCE_PATH = "./src/commonTest/resources"
    }
}

expect class TestSqlDriverFactory(sqlDriverFactory: SqlDriverFactory) : SqlDriverFactory {
    override fun getAsyncSchema(): SqlSchema<QueryResult.AsyncValue<Unit>>
    override fun getSchema(): SqlSchema<QueryResult.Value<Unit>>?
    override suspend fun createDriver(): SqlDriver
}

expect class Resource(name: String) {
    val name: String

    suspend fun exists(): Boolean
    suspend fun readText(): String
}


val isPlatformBrowser get() = getPlatform().type == Platform.Type.BROWSER

fun runTestWithoutBrowser(
    testBody: suspend TestScope.() -> Unit
): TestResult {
    return runTest {
        if (isPlatformBrowser) return@runTest
        testBody()
    }
}

fun runTestWithout(
    vararg platforms: Platform.Type,
    testBody: suspend TestScope.() -> Unit
): TestResult {
    return runTest {
        if (platforms.contains(getPlatform().type)) return@runTest
        testBody()
    }
}

fun runTestOnlyOn(
    vararg platforms: Platform.Type,
    testBody: suspend TestScope.() -> Unit
): TestResult {
    return runTest {
        if (platforms.contains(getPlatform().type))
            testBody()
    }
}