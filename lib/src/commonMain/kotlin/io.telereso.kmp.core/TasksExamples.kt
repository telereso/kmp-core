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

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.telereso.kmp.core.Consumer.Companion.android
import io.telereso.kmp.core.Consumer.Companion.ios
import io.telereso.kmp.core.Consumer.Companion.website
import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.JwtPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.js.JsExport
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

//@JsExport
object TasksExamples {
    @JvmStatic
    fun hi(): Task<String> {
        return Task.execute {
            logDebug("log hi")
            "hi from task!!"
        }
    }

    @JvmStatic
    fun exception(): Task<String> {
        return Task.execute {
            throw ClientException("test")
            "hi from task!!"
        }
    }

    @JvmStatic
    fun hiDelayed(): Task<String> {
        return Task.execute {
            delay(5000)
            "hi from task!!"
        }
    }

    @JvmStatic
    fun apiCall(): Task<String> {
        return Task.execute {
            httpClient {  }.get("https://run.mocky.io/v3/7a7a924f-72dd-4cd7-aefa-12be3608e839").bodyAsText()
        }
    }

    @JvmStatic
    fun getFlow(): CommonFlow<String> {
        return flowOf("a", "b", "c").asCommonFlow()
    }

    @JvmStatic
    fun getFlowPayload(): CommonFlow<JwtPayload> {
        return flowOf(JwtPayload(), JwtPayload(), JwtPayload()).asCommonFlow()
    }

    @JvmStatic
    fun getDelayedFlowString(): CommonFlow<String> {
        val stateFlow = MutableStateFlow("")
        Task.execute {
            delay(1000)
            stateFlow.emit("1")
            delay(2000)
            stateFlow.emit("2")
            delay(3000)
            stateFlow.emit("3")
        }
        return stateFlow.asCommonFlow()
    }

    fun testVerify(coreClient: CoreClient) {

        coreClient.verifyConsumer(mutableListOf<Consumer>().apply {
            add(
                android(
                    "io.telereso.kmp.core.app",
                    "25:2F:51:3C:AA:FD:E2:14:EB:2D:C0:A7:1E:D2:F3:E2:95:5A:BE:7F:BB:CE:D7:8A:F9:CB:BB:C0:5F:0C:12:98"
                )
            )
            add(
                android(
                    "io.telereso.kmp.core.app",
                    "85:2F:51:3C:AA:FD:E2:14:EB:2D:C0:A7:1E:D2:F3:E2:95:5A:BE:7F:BB:CE:D7:8A:F9:CB:BB:C0:5F:0C:12:98"
                )
            )
            add(
                android(
                    "io.telereso.kmp.core.app",
                    "27:CD:DF:48:77:3E:9B:CF:FA:A4:6D:44:BF:8A:FC:23:96:F0:2F:71:C5:79:5C:C9:A0:1B:63:C6:BD:B1:05:6A"
                )
            )
            add(ios("orgIdentifier.iosApp"))
            add(website("localhost:*"))
        })
    }

    fun testVerifyFailed(coreClient: CoreClient) {

        coreClient.verifyConsumer(mutableListOf<Consumer>().apply {
            add(
                android(
                    "io.telereso.kmp.core.app",
                    "25:2F:51:3C:AA:FD:E2:14:EB:2D:C0:A7:1E:D2:F3:E2:95:5A:BE:7F:BB:CE:D7:8A:F9:CB:BB:C0:5F:0C:12:98"
                )
            )
            add(
                android(
                    "io.telereso.kmp.core.app",
                    "85:2F:51:3C:AA:FD:E2:14:EB:2D:C0:A7:1E:D2:F3:E2:95:5A:BE:7F:BB:CE:D7:8A:F9:CB:BB:C0:5F:0C:12:98"
                )
            )
            add(ios("orgIdentifier.iosApp"))
            add(website("localhost:*"))
        })
    }


    @JvmStatic
    fun testRetry1(): Task<String> {
        var count = 0
        return Task.execute(retry = 1) {
            if (count < 1) {
                logDebug("testRetry1 failed $count")
                count++
                throw Throwable("testRetry1 Throwable")
            }
            "testRetry1 Passed!"
        }
    }

    @JvmStatic
    @JvmOverloads
    fun testRetry2(config: TaskConfig? = null): Task<String> {
        var count = 0
        return Task.execute(retry = 2, config = config) {
            if (count < 2) {
                logDebug("testRetry2 failed $count")
                count++
                throw Throwable("testRetry2 Throwable")
            }
            "testRetry2 Passed!"
        }
    }

    @JvmStatic
    fun testRetry3(config: TaskConfig? = null): Task<String> {
        var count = 0
        return Task.execute(retry = 3, startDelay = 3000, backOffDelay = 1000, config = config) {
            if (count < 4) {
                logDebug("testRetry3 failed $count")
                count++
                throw Throwable("testRetry3 Throwable")
            }
            "testRetry3 Passed!"
        }
    }
}