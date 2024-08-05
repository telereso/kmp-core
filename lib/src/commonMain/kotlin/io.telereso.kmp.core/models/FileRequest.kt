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

package io.telereso.kmp.core.models

import io.ktor.http.ContentType
import io.ktor.http.fromFileExtension
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.JsExport
import kotlin.js.JsName

@Serializable
@JsExport
data class FileRequest(
    /**
     *  Base64 encoding of file
     */
    val base64: String,
    /**
     * Name of the uploaded file with extension
     */
    val name: String,
    /**
     * The file content type, eg "image/png", for full list of type you can check [ContentType]
     * ByDefault
     */
    val contentType: String = name.contentType().toString(),
    /**
     * Upload progress call back, make sure to invoke and calculate with ktor's onUpload
     */
    val progress: ((percentage: Int) -> Unit) = {}
) {

    @JsName("withContentType")
    constructor(base64: String, name: String, contentType: String) : this(base64, name, contentType,{})

    @JsName("withName")
    constructor(base64: String, name: String) : this(base64, name, progress = {})

    @JsName("withProgress")
    constructor(base64: String, name: String, progress: (percentage: Int) -> Unit) : this(
        base64,
        name,
        contentType = name.contentType().toString(),
        progress = progress
    )

    fun getType(): ContentType {
        return ContentType.parse(contentType)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun getByteArray(): ByteArray {
        return Base64.decode(base64)
    }
}

fun String.contentType(): ContentType {
    return ContentType.fromFileExtension(this.split(".").last()).first()
}