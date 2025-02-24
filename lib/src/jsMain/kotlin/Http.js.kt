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

import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readText
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.xhr.FormData


fun setupHttpClient(platform: String) {
    if (platform == "web") return
//    js("globalThis.fetch = global.fetch")


//    CoreHttpClient.current = object : CoreHttpClient {
//        val requestTime = Clock.System.now().epochSeconds

//        override fun request(
//            url: String,
//            method: String,
//            headers: Map<String, List<String>>,
//            body: String?
//        ): Task<CoreHttpResponse> {
//            return Task.execute {
//                runCatching {
//                    val responseTime = Clock.System.now().epochSeconds
//                    val res = fetch(
//                        input = url,
//                        init = object : RequestInit {
//                            override var cache: dynamic
//                                get() = super.cache
//                                set(value) {}
//
//                            override var credentials: dynamic
//                                get() = super.credentials
//                                set(value) {}
//
//                            override var method: String?
//                                get() = method
//                                set(value) {}
//
//                            override var headers: Headers
//                                get() = headers.let {
//                                    Headers().apply {
//                                        it.forEach { e ->
//                                            e.value.forEach { v -> append(e.key, v) }
//                                        }
//                                    }
//                                }
//                                set(value) {}
//
//                            override var body: dynamic
//                                get() = body
//                                set(value) {}
//                        }
//                    ).await()
//                    CoreHttpResponse(
//                        requestTimeUnix = requestTime.toDouble(),
//                        responseTimeUnix = responseTime.toDouble(),
//                        httpStatus = res.status.toInt(),
//                        bodyString = res.text().await()
//                    )
//                }.getOrElse {
//                    throw it
//                }
//            }
//        }
//    }
}

suspend fun fetchFromKtorRequest(
    requestData: HttpRequestData,
    multipartBody: FormData? = null // Pass FormData manually for multipart requests
): String {
    val url = requestData.url.toString()
    val method = requestData.method.value
    val headers = Headers()

    requestData.headers.forEach { key, values ->
        values.forEach { value -> headers.append(key, value) }
    }

    val contentType = requestData.headers[HttpHeaders.ContentType]
    var body: dynamic = null

    when {
        // Handle JSON Body
        contentType?.startsWith(ContentType.Application.Json.toString()) == true -> {
            body = (requestData.body as? ByteReadChannel)?.readRemaining()?.readText()
        }

        // Handle Multipart FormData (Body must be provided separately)
        contentType?.startsWith(ContentType.MultiPart.FormData.toString()) == true -> {
            body = multipartBody ?: throw IllegalArgumentException("Multipart body must be provided separately.")
        }

        // Handle other body types (if needed)
        requestData.body is ByteReadChannel -> {
            body = (requestData.body as ByteReadChannel).readRemaining().readText()
        }
    }

    val response = window.fetch(
        url,
        RequestInit(
            method = method,
            headers = headers,
            body = body
        )
    ).await()

    return response.text().await()
}

suspend fun HttpRequestData.toFormData(): FormData {
    val formData = FormData()

    if (body is MultiPartFormDataContent) {
        val multipartContent = body as MultiPartFormDataContent

//        multipartContent.parts
//            .toList() // Convert Flow<PartData> to List<PartData>
//            .forEach { part ->
//                when (part) {
//                    is PartData.FormItem -> {
//                        formData.append(part.name ?: "", part.value)
//                    }
//
//                    is PartData.FileItem -> {
//                        val bytes = part.provider().readBytes()
//                        val blob = Blob(arrayOf(bytes), BlobPropertyBag(type = part.contentType?.toString()))
//                        formData.append(part.name ?: "", blob, part.originalFileName ?: "file")
//                    }
//
//                    else -> {}
//                }
//            }
    }

    return formData
}