///*
// * MIT License
// *
// * Copyright (c) 2023 Telereso
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package io.telereso.kmp.core
//
//import io.telereso.kmp.core.Http.asClientException
//import io.kotest.assertions.throwables.shouldThrow
//import io.kotest.matchers.booleans.shouldBeFalse
//import io.kotest.matchers.booleans.shouldBeTrue
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.string.shouldContain
//import io.kotest.matchers.types.shouldBeInstanceOf
//import io.ktor.client.*
//import io.ktor.client.engine.mock.*
//import io.ktor.client.plugins.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.request.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import io.telereso.kmp.core.Http.hasStatus
//import io.telereso.kmp.core.Http.successful
//import io.telereso.kmp.core.models.ClientException
//import io.telereso.kmp.core.models.ErrorBody
//import io.telereso.kmp.core.models.asClientException
//import io.telereso.kmp.core.models.toJson
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import kotlin.test.Test
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class HttpTest {
//
//    @Test
//    fun shouldReturnProperClientExceptionGivenHttpResponse() = runTest {
//        val errorBody = ErrorBody(code = "TESTING", message = "testing").toJson()
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler {
//                    respond(
//                        errorBody,
//                        HttpStatusCode.BadRequest,
//                        headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
//                    )
//                }
//            }
//            install(ContentNegotiation) {
//                json(Http.ktorConfigJson)
//            }
//        }
//
//        val httpResponse = client.post {
//            url {
//                protocol = URLProtocol.HTTPS
//                host = "example.com"
//                path("hello")
//                parameter("clientId", "clientId")
//            }
//        }
//
//        httpResponse.asClientException(cause = NullPointerException("sample setting cause")).run {
//            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value)
//            errorBody.shouldBe(errorBody)
//            errorType.shouldBe("HTTP")
//            message.shouldBe("testing")
//            cause?.message.shouldBe("sample setting cause")
//        }
//
//        httpResponse.asClientException(message = "SomeMessage").run {
//            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value)
//            errorBody.shouldBe(errorBody)
//            errorType.shouldBe("HTTP")
//            message.shouldBe("SomeMessage")
//        }
//    }
//
//    @Test
//    fun shouldThrowClientExceptionGivenFailed() = runTest {
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler {
//                    respond("", HttpStatusCode.BadRequest)
//                }
//            }
//            install(ContentNegotiation) {
//                json(Http.ktorConfigJson)
//            }
//        }
//
//        val httpResponse = client.post {
//            url {
//                protocol = URLProtocol.HTTPS
//                host = "example.com"
//                path("hello")
//                parameter("clientId", "clientId")
//            }
//        }
//
//        httpResponse.asClientException().shouldBeInstanceOf<ClientException>()
//    }
//
//    @Test
//    fun protocolGivenHTTPValue() {
//        Http.protocol("http").shouldBe(URLProtocol.HTTP)
//    }
//
//    @Test
//    fun protocolGivenHTTPSValue() {
//        Http.protocol("https").shouldBe(URLProtocol.HTTPS)
//    }
//
//    @Test
//    fun protocolGivenNullValue() {
//        // should return a HTTPS protocol
//        Http.protocol(null).shouldBe(URLProtocol.HTTPS)
//    }
//
//    @Test
//    fun protocolGivenFakeValue() {
//        // should return a HTTPS protocol given fake value
//        Http.protocol("fake").shouldBe(URLProtocol.HTTPS)
//
//        Http.protocol("").shouldBe(URLProtocol.HTTPS)
//    }
//
//    @Test
//    fun ktorConfigJson() {
//        Http.ktorConfigJson.configuration.prettyPrint.shouldBeFalse()
//        Http.ktorConfigJson.configuration.isLenient.shouldBeTrue()
//        Http.ktorConfigJson.configuration.ignoreUnknownKeys.shouldBeTrue()
//    }
//
//    @Test
//    fun connectionTime() {
//        Http.CONNECTION_TIME_OUT_MILLIS.shouldBe(30000L)
//        Http.REQUEST_TIME_OUT_MILLIS.shouldBe(30000L)
//    }
//
//    @Test
//    fun testGetUserAgentAndroid() = runTest {
//        val androidPlatform = object : Platform() {
//            override val type: Platform.Type
//                get() = Platform.Type.ANDROID
//            override val userAgent: String
//                get() = "sdk/test device/test"
//
//        }
//
//        Http.getUserAgent(
//            androidPlatform,
//            "TestClient",
//            null,
//            null, null
//        ).shouldBe("NA/NA TestClient/NA sdk/test device/test")
//
//
//        Http.getUserAgent(
//            androidPlatform,
//            "TestClient",
//            "1.0.0",
//            null, "1.0.1"
//        ).shouldBe("NA/1.0.1 TestClient/1.0.0 sdk/test device/test")
//
//    }
//
//    @Test
//    fun testGetUserAgentIos() = runTest {
//        val iosPlatform = object : Platform() {
//            override val type: Platform.Type
//                get() = Platform.Type.IOS
//            override val userAgent: String
//                get() = "sdk/test device/test"
//
//        }
//
//        Http.getUserAgent(
//            iosPlatform,
//            "TestClient",
//            null,
//            null, null
//        ).shouldBe("NA/NA TestClient/NA sdk/test device/test")
//
//
//        Http.getUserAgent(
//            iosPlatform,
//            "TestClient",
//            "1.0.0",
//            null, "1.0.1"
//        ).shouldBe("NA/1.0.1 TestClient/1.0.0 sdk/test device/test")
//
//    }
//
//    @Test
//    fun testGetUserAgentJvm() = runTest {
//        val jvmPlatform = object : Platform() {
//            override val type: Platform.Type
//                get() = Platform.Type.JVM
//            override val userAgent: String
//                get() = "sdk/test device/test"
//
//        }
//
//        Http.getUserAgent(
//            jvmPlatform,
//            "TestClient",
//            null,
//            null, null
//        ).shouldBe("NA/NA TestClient/NA sdk/test device/test")
//
//
//        Http.getUserAgent(
//            jvmPlatform,
//            "TestClient",
//            "1.0.0",
//            null, "1.0.1"
//        ).shouldBe("NA/1.0.1 TestClient/1.0.0 sdk/test device/test")
//
//    }
//
//    @Test
//    fun testGetUserAgentJs() = runTest {
//        val jsPlatform = object : Platform() {
//            override val type: Platform.Type
//                get() = Platform.Type.BROWSER
//            override val userAgent: String
//                get() = "sdk/test device/test"
//
//        }
//
//        Http.getUserAgent(
//            jsPlatform,
//            "TestClient",
//            null,
//            null, null
//        ).shouldBe("NA/NA TestClient/NA sdk/test device/test")
//
//
//        Http.getUserAgent(
//            jsPlatform,
//            "TestClient",
//            "1.0.0",
//            null, "1.0.1"
//        ).shouldBe("NA/1.0.1 TestClient/1.0.0 sdk/test device/test")
//
//    }
//
//    @Test
//    fun shouldReturnTrueIfResponseIsSuccessful() = runTest {
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler {
//                    respond("", HttpStatusCode.Accepted)
//                }
//            }
//            install(ContentNegotiation) {
//                json(Http.ktorConfigJson)
//            }
//        }
//
//        val httpResponse = client.post {
//            url {
//                protocol = URLProtocol.HTTPS
//                host = "example.com"
//                path("hello")
//                parameter("clientId", "clientId")
//            }
//        }
//
//        httpResponse.successful().shouldBe(true)
//    }
//
//    @Test
//    fun shouldReturnFalseIfResponseFails() = runTest {
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler {
//                    respond("", HttpStatusCode.BadRequest)
//                }
//            }
//            install(ContentNegotiation) {
//                json(Http.ktorConfigJson)
//            }
//        }
//
//        val httpResponse = client.post {
//            url {
//                protocol = URLProtocol.HTTPS
//                host = "example.com"
//                path("hello")
//                parameter("clientId", "clientId")
//            }
//        }
//
//        httpResponse.successful().shouldBe(false)
//    }
//
//    @Test
//    fun shouldReturnTrueIfStatusCodeIsPresent() = runTest {
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler {
//                    respond("", HttpStatusCode.Accepted)
//                }
//            }
//            install(ContentNegotiation) {
//                json(Http.ktorConfigJson)
//            }
//        }
//
//        val httpResponse = client.post {
//            url {
//                protocol = URLProtocol.HTTPS
//                host = "example.com"
//                path("hello")
//                parameter("clientId", "clientId")
//            }
//        }
//
//        httpResponse.hasStatus(HttpStatusCode.Accepted).shouldBe(true)
//    }
//
//    @Test
//    fun shouldReturnFalseIfStatusCodeIsNotPresent() = runTest {
//        val client = HttpClient(MockEngine) {
//            engine {
//                addHandler {
//                    respond("", HttpStatusCode.Accepted)
//                }
//            }
//            install(ContentNegotiation) {
//                json(Http.ktorConfigJson)
//            }
//        }
//
//        val httpResponse = client.post {
//            url {
//                protocol = URLProtocol.HTTPS
//                host = "example.com"
//                path("hello")
//                parameter("clientId", "clientId")
//            }
//        }
//
//        httpResponse.hasStatus(HttpStatusCode.BadRequest).shouldBe(false)
//    }
//}