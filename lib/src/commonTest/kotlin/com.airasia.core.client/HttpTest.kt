package com.airasia.core.client

import com.airasia.core.client.Http.asClientException
import io.kotest.assertions.throwables.shouldThrow
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HttpTest {

    @Test
    fun shouldReturnProperClientExceptionGivenHttpResponse() = runTest {
        val errorBody = ApiErrorBody(code = "TESTING", message = "testing").toJson()
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
                host = "airasia.com"
                path("hello")
                parameter("clientId", "clientId")
            }
        }

        httpResponse.asClientException(cause = NullPointerException("sample setting cause")).run {
            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value.toString())
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("TESTING")
            message.shouldBe("HttpResponse[https://airasia.com/hello?clientId=clientId, 400 Bad Request]")
            cause?.message.shouldBe("sample setting cause")
        }

        httpResponse.asClientException(message = "SomeMessage").run {
            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value.toString())
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("TESTING")
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
                host = "airasia.com"
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
    fun shouldGetHeaders() {
        Http.getHeaders(
            origin = "origin",
            "apiKey",
            "clientID",
            "accessToken",
            ContentType.Application.Json
        ).run {
            size.shouldBe(5)
            shouldContain(Pair("Origin", "origin"))
            shouldContain(Pair("X-Api-Key", "apiKey"))
            shouldContain(Pair("Content-Type", "application/json"))
            shouldContain(Pair("X-Aa-Client-Id", "clientID"))
            shouldContain(Pair("Authorization", "accessToken"))
        }
    }

    @Test
    fun shouldGetEmptyHeadersGivenNullOrEmptyValues() {
        Http.getHeaders().run {
            shouldBeEmpty()
        }

        // atleasr one
        Http.getHeaders(contentType = ContentType.Application.Json).run {
            shouldNotBeEmpty()
            shouldContain(Pair("Content-Type", "application/json"))
        }

        Http.getHeaders(origin = "").run {
            shouldBeEmpty()
        }

        Http.getHeaders(apiKey = "").run {
            shouldBeEmpty()
        }

        Http.getHeaders(accessToken = "").run {
            shouldBeEmpty()
        }

        Http.getHeaders(client_id = "").run {
            shouldBeEmpty()
        }
    }

    @Test
    fun getHttpResponseValidatorForKtorClientException() = runTest {
        val errorBody = ApiErrorBody(code = "TESTING", message = "testing").toJson()
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
                    host = "airasia.com"
                    path("hello")
                    parameter("clientId", "clientId")
                }
            }
        }.run {
            httpStatusCode.shouldBe(HttpStatusCode.BadRequest.value.toString())
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("TESTING")
            this.toClientException().message.shouldContain("ClientRequestException: Client request(POST https://airasia.com/hello?clientId=clientId) invalid: 400 Bad Request. Text: \"{\"code\":\"TESTING\",\"message\":\"testing\"}\"")
        }
    }

    @Test
    fun getHttpResponseValidatorForKtorServerException() = runTest {
        val errorBody =
            ApiErrorBody(code = "SERVER_ERROR", message = "internal server error").toJson()
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
                    host = "airasia.com"
                    path("hello")
                    parameter("clientId", "clientId")
                }
            }
        }.run {
            httpStatusCode.shouldBe(HttpStatusCode.InternalServerError.value.toString())
            errorBody.shouldBe(errorBody)
            errorType.shouldBe("SERVER_ERROR")
            message.shouldContain("ServerResponseException: Server error(POST https://airasia.com/hello?clientId=clientId: 500 Internal Server Error. Text: \"{\"code\":\"SERVER_ERROR\",\"message\":\"internal server error\"}\"")
        }
    }

    @Test
    fun ktorConfigJson() {
        Http.ktorConfigJson.configuration.prettyPrint.shouldBeTrue()
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