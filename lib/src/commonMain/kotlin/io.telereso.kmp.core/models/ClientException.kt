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

import io.ktor.client.plugins.ResponseException
import io.telereso.kmp.core.Http
import io.telereso.kmp.core.Http.asClientException
import io.telereso.kmp.core.ThrowableSerializer
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * ClientException class wraps exception and throwable across the code base.
 * We encourage to use this class when dealing with Http Error response or throwing exceptions .
 * @param httpStatusCode the API code returned e.g 500, 400
 * @param errorBody The error returned as a Json string.
 * @param message the message related to this Exception.
 * @param cause the cause related to this exception.
 * @param errorType the string error type returned from API/ e.g INVALID_USERNAME_PASSWORD
 */
@Serializable
@ExperimentalJsExport
//@JsExport
open class ClientException(
    override val message: String? = null,
    @Serializable(with = ThrowableSerializer::class)
    override val cause: Throwable? = null,
    val httpStatusCode: Int? = null,
    val errorBody: ErrorBody? = null,
    val errorString: String? = null,
    val errorType: String? = null,
    val httpURl: String? = null,
    val code: String? = null,
    val failureCount: Int? = null
) : Throwable() {
//    @JsName("ClientExceptionConstructor")
    constructor(cause: Throwable? = null) : this(null, cause = cause)

    fun isCancellationException(): Boolean {
        return cause is CancellationException
    }
    /**
     * class companion class
     */
    companion object {
        /**
         * Expose a global listener to use with third party analytics and crashlytics.
         */
        var listener: ((t: Throwable) -> Unit) = { }
    }

        /**
     * Custom implementation of the toString() function.
     *
     * @return a json string representation of the object.
     * If the object can't be serialized to json, it returns the default toString() implementation
     */
    override fun toString(): String {
        return this.toJsonPretty().ifEmpty { super.toString() }
    }
}

/**
 * function that takes a string as input and returns the [ErrorBody] of a given error message.
 *
 * @param body : a string that contains the error details.
 *
 * @return : a json string that contains the [ErrorBody]
 *
 * The function uses the Http.ktorConfigJson object to decode the input string as an ErrorBody object,
 * and then it returns the code field of the object
 */
fun getErrorBody(body: String): ErrorBody {
    return Http.ktorConfigJson.decodeFromString(body)
}

/**
 * provides a safe casting of throwable
 * converts any throwable into a [ClientException].
 * Clients don't need to perform casting as its handled on the SDK level.
 * @return a [ClientException] object when casting success else it will return a [ClientException] with the throwable as a cause
 */
fun Throwable.asClientException(failureCount: Int = 0): ClientException {
    return if (this as? ClientException != null) this else ClientException(
        cause = this,
        message = this.message,
        errorType = "Throwable",
        failureCount = failureCount
    )
}

/**
 * provides a safe suspended casting of throwable
 * converts any throwable into a [ClientException].
 * Clients don't need to perform casting as its handled on the SDK level.
 * @return a [ClientException] object when casting success else it will return a [ClientException] with the throwable as a cause
 */
suspend fun Throwable.toClientException(failureCount: Int = 0): ClientException {
    return when (this) {
        is ResponseException -> response.asClientException()
        else -> asClientException(failureCount)
    }
}


