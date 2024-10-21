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

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.http.ContentType
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.ErrorBody
import io.telereso.kmp.core.models.ErrorBodyBuilder
import io.telereso.kmp.core.models.ExpirableValue
import io.telereso.kmp.core.models.FileRequest
import io.telereso.kmp.core.models.JwtPayload
import io.telereso.kmp.core.models.asClientException
import io.telereso.kmp.core.models.fromJson
import io.telereso.kmp.core.models.fromJsonArray
import io.telereso.kmp.core.models.`object`
import io.telereso.kmp.core.models.toJson
import io.telereso.kmp.core.models.toJsonPretty
import kotlin.test.Test

class ModelsTest {

    @Test
    fun testClientException() {
        val t = kotlin.Throwable("test")
        val ce = t.asClientException(1)
        val throwableString = when (getPlatform().type) {
            Platform.Type.ANDROID, Platform.Type.JVM -> "java.lang.Throwable"
            Platform.Type.IOS -> "kotlin.Throwable"
            Platform.Type.BROWSER, Platform.Type.NODE -> "Throwable"
        }

        ClientException(t).toString().shouldBe(
            """
            {
                "cause": "$throwableString: test"
            }
        """.trimIndent()
        )

        ce.toString().shouldBe(
            """
            {
                "message": "test",
                "cause": "$throwableString: test",
                "errorType": "Throwable",
                "failureCount": 1
            }
        """.trimIndent()
        )

        ce.toJson()
            .shouldBe("{\"message\":\"test\",\"cause\":\"$throwableString: test\",\"errorType\":\"Throwable\",\"failureCount\":1}")

        ce.toJsonPretty().shouldBe(
            """
            {
                "message": "test",
                "cause": "$throwableString: test",
                "errorType": "Throwable",
                "failureCount": 1
            }
        """.trimIndent()
        )

        ClientException.fromJson(
            """
            {
                "message": "test",
                "cause": "$throwableString: test",
                "errorType": "Throwable",
                "failureCount": 1
            }
        """
        ).shouldBe(ce)

        ClientException.fromJsonArray(
            """
            [
              {
                "message": "test",
                "cause": "$throwableString: test",
                "errorType": "Throwable",
                "failureCount": 1
              },
              {
                "message": "test",
                "cause": "$throwableString: test",
                "errorType": "Throwable",
                "failureCount": 1
              }
            ]
        """.trimIndent()
        ).shouldBe(arrayOf(ce, ce))

        ClientException.toJson(arrayOf(ce, ce)).shouldBe(
            """
            [{"message":"test","cause":"$throwableString: test","errorType":"Throwable","failureCount":1},{"message":"test","cause":"$throwableString: test","errorType":"Throwable","failureCount":1}]
        """.trimIndent()
        )
    }

    @Test
    fun testErrorBody() {
        val eb = ErrorBodyBuilder.builder().message("test").code("test").build()

        ErrorBody.Companion.`object`().toJson().shouldBe("""{}""".trimIndent())
        ErrorBody.Companion.`object`("test").toJson().shouldBe("""{"code":"test"}""".trimIndent())
        ErrorBody.Companion.`object`("test", "test").toJson()
            .shouldBe("""{"code":"test","message":"test"}""".trimIndent())

        eb.shouldBe(ErrorBody("test", "test"))

        eb.toJson().shouldBe("{\"code\":\"test\",\"message\":\"test\"}")

        eb.toJsonPretty().shouldBe(
            """
            {
                "code": "test",
                "message": "test"
            }
        """.trimIndent()
        )

        ErrorBody.fromJson(
            """
            {
                "code": "test",
                "message": "test"
            }
        """
        ).shouldBe(eb)

        ErrorBody.fromJsonArray(
            """
            [
              {
                "code": "test",
                "message": "test"
              },
              {
                "code": "test",
                "message": "test"
              }
            ]
        """.trimIndent()
        ).shouldBe(arrayOf(eb, eb))

        ErrorBody.toJson(arrayOf(eb, eb)).shouldBe(
            """
            [{"code":"test","message":"test"},{"code":"test","message":"test"}]
        """.trimIndent()
        )

    }

    @Test
    fun testExpirableValue() {
        val ev = ExpirableValue("test", 1)

        ev.toJson().shouldBe("""{"value":"test","exp":1}""".trimIndent())

        ev.toJsonPretty().shouldBe(
            """
            {
                "value": "test",
                "exp": 1
            }
        """.trimIndent()
        )

        ExpirableValue.fromJson(
            """
            {
                "value": "test",
                "exp": 1
            }
        """
        ).shouldBe(ev)

        ExpirableValue.fromJsonArray(
            """
            [
              {
                "value": "test",
                "exp": 1
              },
              {
                "value": "test",
                "exp": 1
              }
            ]
        """.trimIndent()
        ).shouldBe(arrayOf(ev, ev))

        ExpirableValue.toJson(arrayOf(ev, ev)).shouldBe(
            """
            [{"value":"test","exp":1},{"value":"test","exp":1}]
        """.trimIndent()
        )
    }

    @Test
    fun testFileRequest() {
        val progress : ((percentage: Int) -> Unit) = { p ->
            p.shouldBe(10)
        }

        val fr = FileRequest("dGVzdA==", "test.png", "image/png", progress)

        FileRequest("dGVzdA==", "test.png", "image/png")
            .toJson()
            .shouldBe("""{"base64":"dGVzdA==","name":"test.png","contentType":"image/png"}""".trimIndent())

        FileRequest("dGVzdA==", "test.png")
            .toJson()
            .shouldBe("""{"base64":"dGVzdA==","name":"test.png","contentType":"image/png"}""".trimIndent())

        FileRequest("dGVzdA==", "test.png", progress = progress)
            .toJson()
            .shouldBe("""{"base64":"dGVzdA==","name":"test.png","contentType":"image/png"}""".trimIndent())

        fr.progress.invoke(10)
        fr.contentType.shouldBe("image/png")
        fr.getType().shouldBe(ContentType.Image.PNG)
        fr.getByteArray().decodeToString().shouldBe("test")

        fr.toJsonPretty().shouldBe(
            """
            {
                "base64": "dGVzdA==",
                "name": "test.png",
                "contentType": "image/png"
            }
        """.trimIndent()
        )

        FileRequest.fromJson(
            """
            {
                "base64": "dGVzdA==",
                "name": "test.png",
                "contentType": "image/png"
            }
        """
        ).copy(progress = progress).shouldBe(fr)

        FileRequest.fromJsonArray(
            """
            [
              {
                "base64": "dGVzdA==",
                "name": "test.png",
                "contentType": "image/png"
              },
              {
                "base64": "dGVzdA==",
                "name": "test.png",
                "contentType": "image/png"
              }
            ]
        """.trimIndent()
        ).map { it.copy(progress = progress) }.shouldBe(arrayOf(fr, fr))

        FileRequest.toJson(arrayOf(fr, fr)).shouldBe(
            """
            [{"base64":"dGVzdA==","name":"test.png","contentType":"image/png"},{"base64":"dGVzdA==","name":"test.png","contentType":"image/png"}]
        """.trimIndent()
        )


    }

    @Test
    fun testJwtPayload() {
        val jp = JwtPayload("test",1,1,"test")

        jp.toJson().shouldBe("""{"iss":"test","exp":1,"iat":1,"sub":"test"}""".trimIndent())
        jp.toJsonPretty().shouldBe(
            """
            {
                "iss": "test",
                "exp": 1,
                "iat": 1,
                "sub": "test"
            }
        """.trimIndent()
        )

        JwtPayload.fromJson(
            """
            {
                "iss": "test",
                "exp": 1,
                "iat": 1,
                "sub": "test"
            }
        """
        ).shouldBe(jp)

        JwtPayload.fromJsonArray(
            """
            [
              {
                "iss": "test",
                "exp": 1,
                "iat": 1,
                "sub": "test"
              },
              {
                "iss": "test",
                "exp": 1,
                "iat": 1,
                "sub": "test"
              }
            ]
        """.trimIndent()
        ).shouldBe(arrayOf(jp, jp))

        JwtPayload.toJson(arrayOf(jp, jp)).shouldBe(
            """
            [{"iss":"test","exp":1,"iat":1,"sub":"test"},{"iss":"test","exp":1,"iat":1,"sub":"test"}]
        """.trimIndent()
        )
    }
}
