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
import app.cash.sqldelight.driver.native.inMemoryDriver
import io.telereso.kmp.core.SqlDriverFactory
import kotlinx.cinterop.*
import platform.posix.*

import platform.Foundation.NSBundle

@OptIn(ExperimentalForeignApi::class)
actual class Resource actual constructor(actual val name: String) {
    val pathParts = name.split("[.|/]".toRegex())
    val path = NSBundle.mainBundle.pathForResource("resources/${pathParts[0]}", pathParts[1])
    private val file: CPointer<FILE>? = fopen(path, "r")
    actual suspend fun exists(): Boolean = file != null

    actual suspend fun readText(): String {
        fseek(file, 0, SEEK_END)
        val size = ftell(file)
        rewind(file)

        return memScoped {
            val tmp = allocArray<ByteVar>(size)
            fread(tmp, sizeOf<ByteVar>().convert(), size.convert(), file)
            tmp.toKString()
        }
    }

    actual suspend fun writeText(text:String) {}
}

actual class TestSqlDriverFactory actual constructor(
    val sqlDriverFactory: SqlDriverFactory,
    overrideName: Boolean
) : SqlDriverFactory(sqlDriverFactory.databaseName(overrideName)) {
    actual override fun getAsyncSchema() = sqlDriverFactory.getAsyncSchema()
    actual override fun getSchema() = sqlDriverFactory.getSchema()
    actual override suspend fun createDriver(): SqlDriver = inMemoryDriver(getSchema()!!)
}
