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

import io.telereso.kmp.core.Http.asClientException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.ErrorBody
import io.telereso.kmp.core.models.asClientException
import io.telereso.kmp.core.models.toJson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HttpTest {

    @Test
    fun shouldReturnProperClientExceptionGivenHttpResponse() = runTest {
        val errorBody = ErrorBody(code = "TESTING", message = "testing").toJson()
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        errorBody,
                        HttpStatusCode.BadRequest,
                        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
            install(ContentNegotiation) {
                json(Http.ktorConfigJson)
            }
        }

        val httpResponse = client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = "example.com"
                path("hello")
                parameter("clientId", "clientId")
            }
        }

        httpResponse.asClientException(cause = NullPointerException("sample setting cause")).run {
            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value)
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("HTTP")
            message.shouldBe("testing")
            cause?.message.shouldBe("sample setting cause")
        }

        httpResponse.asClientException(message = "SomeMessage").run {
            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value)
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("HTTP")
            message.shouldBe("SomeMessage")
        }
    }

    @Test
    fun shouldThrowClientExceptionGivenFailed() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond("", HttpStatusCode.BadRequest)
                }
            }
            install(ContentNegotiation) {
                json(Http.ktorConfigJson)
            }
        }

        val httpResponse = client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = "example.com"
                path("hello")
                parameter("clientId", "clientId")
            }
        }

        shouldThrow<ClientException> {
            httpResponse.asClientException()
        }
    }

    @Test
    fun protocolGivenHTTPValue() {
        Http.protocol("http").shouldBe(URLProtocol.HTTP)
    }

    @Test
    fun protocolGivenHTTPSValue() {
        Http.protocol("https").shouldBe(URLProtocol.HTTPS)
    }

    @Test
    fun protocolGivenNullValue() {
        // should return a HTTPS protocol
        Http.protocol(null).shouldBe(URLProtocol.HTTPS)
    }

    @Test
    fun protocolGivenFakeValue() {
        // should return a HTTPS protocol given fake value
        Http.protocol("fake").shouldBe(URLProtocol.HTTPS)

        Http.protocol("").shouldBe(URLProtocol.HTTPS)
    }
    @Test
    fun getHttpResponseValidatorForKtorClientException() = runTest {
        val errorBody = ErrorBody(code = "TESTING", message = "testing").toJson()
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        errorBody,
                        HttpStatusCode.BadRequest,
                        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
            install(ContentNegotiation) {
                json(Http.ktorConfigJson)
            }
        }.config {
            expectSuccess = true
            HttpResponseValidator {
                Http.getHttpResponseValidator(this)
            }
        }

        shouldThrow<ClientException> {
            client.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "example.com"
                    path("hello")
                    parameter("clientId", "clientId")
                }
            }
        }.run {
            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value)
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("HTTP")
            this.asClientException().message.shouldContain("testing")
        }
    }

    @Test
    fun getHttpResponseValidatorForKtorServerException() = runTest {
        val errorBody =
            ErrorBody(code = "SERVER_ERROR", message = "internal server error").toJson()
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        errorBody,
                        HttpStatusCode.InternalServerError,
                        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
            install(ContentNegotiation) {
                json(Http.ktorConfigJson)
            }
        }.config {
            expectSuccess = true
            HttpResponseValidator {
                Http.getHttpResponseValidator(this)
            }
        }

        shouldThrow<ClientException> {
            client.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "example.com"
                    path("hello")
                    parameter("clientId", "clientId")
                }
            }
        }.run {
            httpStatusCode.shouldBe(HttpStatusCode.InternalServerError.value)
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("HTTP")
            message.shouldContain("internal server error")
        }
    }


    @Test
    fun getHttpResponseValidatorForKtorServerExceptionWhenErrorBodyMessageNull() = runTest {
        val errorBody =
            ErrorBody(code = "SERVER_ERROR", message = null).toJson()
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        errorBody,
                        HttpStatusCode.InternalServerError,
                        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    )
                }
            }
            install(ContentNegotiation) {
                json(Http.ktorConfigJson)
            }
        }.config {
            expectSuccess = true
            HttpResponseValidator {
                Http.getHttpResponseValidator(this)
            }
        }

        shouldThrow<ClientException> {
            client.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "example.com"
                    path("hello")
                    parameter("clientId", "clientId")
                }
            }
        }.run {
            httpStatusCode.shouldBe(HttpStatusCode.InternalServerError.value)
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("HTTP")
            message.shouldContain("Server error(POST https://example.com/hello?clientId=clientId: 500 Internal Server Error. Text: \"{\"code\":\"SERVER_ERROR\"")
        }
    }

    @Test
    fun ktorConfigJson() {
        Http.ktorConfigJson.configuration.prettyPrint.shouldBeFalse()
        Http.ktorConfigJson.configuration.isLenient.shouldBeTrue()
        Http.ktorConfigJson.configuration.ignoreUnknownKeys.shouldBeTrue()
    }

    @Test
    fun connectionTime() {
        Http.CONNECTION_TIME_OUT_MILLIS.shouldBe(30000L)
        Http.REQUEST_TIME_OUT_MILLIS.shouldBe(30000L)
    }

    @Test
    fun testGetUserAgentAndroid() = runTest {
        val androidPlatform = object :Platform{
            override val type: Platform.TYPE
                get() = Platform.TYPE.ANDROID
            override val name: String
                get() = "sdk/test device/test"

        }

        Http.getUserAgent(
            androidPlatform,
            "TestClient",
            null,
            null
        ).shouldBe("android/NA TestClient/NA sdk/test device/test")


        Http.getUserAgent(
            androidPlatform,
            "TestClient",
            "1.0.0",
            "1.0.1"
        ).shouldBe("android/1.0.1 TestClient/1.0.0 sdk/test device/test")

    }

    @Test
    fun testGetUserAgentIos() = runTest {
        val iosPlatform = object :Platform{
            override val type: Platform.TYPE
                get() = Platform.TYPE.IOS
            override val name: String
                get() = "sdk/test device/test"

        }

        Http.getUserAgent(
            iosPlatform,
            "TestClient",
            null,
            null
        ).shouldBe("ios/NA TestClient/NA sdk/test device/test")


        Http.getUserAgent(
            iosPlatform,
            "TestClient",
            "1.0.0",
            "1.0.1"
        ).shouldBe("ios/1.0.1 TestClient/1.0.0 sdk/test device/test")

    }

    @Test
    fun testGetUserAgentJvm() = runTest {
        val jvmPlatform = object :Platform{
            override val type: Platform.TYPE
                get() = Platform.TYPE.JVM
            override val name: String
                get() = "sdk/test device/test"

        }

        Http.getUserAgent(
            jvmPlatform,
            "TestClient",
            null,
            null
        ).shouldBe("jvm/NA TestClient/NA sdk/test device/test")


        Http.getUserAgent(
            jvmPlatform,
            "TestClient",
            "1.0.0",
            "1.0.1"
        ).shouldBe("jvm/1.0.1 TestClient/1.0.0 sdk/test device/test")

    }

    @Test
    fun testGetUserAgentJs() = runTest {
        val jsPlatform = object :Platform{
            override val type: Platform.TYPE
                get() = Platform.TYPE.JS
            override val name: String
                get() = "sdk/test device/test"

        }

        Http.getUserAgent(
            jsPlatform,
            "TestClient",
            null,
            null
        ).shouldBe("js/NA TestClient/NA sdk/test device/test")


        Http.getUserAgent(
            jsPlatform,
            "TestClient",
            "1.0.0",
            "1.0.1"
        ).shouldBe("js/1.0.1 TestClient/1.0.0 sdk/test device/test")

    }
}