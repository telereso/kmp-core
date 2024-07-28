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

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.asClientException
import io.telereso.kmp.core.models.getErrorBody
import kotlinx.serialization.json.Json

/**
 * a singleton class that holds utils common to the Http.
 */
object Http {
    /**
     * API's connection time out in milli seconds
     */
    const val CONNECTION_TIME_OUT_MILLIS = 30000L

    /**
     * API's request time out in milli seconds
     */
    const val REQUEST_TIME_OUT_MILLIS = 30000L

    /**
     * sets up Ktor Json Configuration.
     */
    val ktorConfigJson: Json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * To build a user agent that provide information where this sdk is being used,
     * on client level pass app-version. here we can read the client version.
     * on client sdk level pass client-name and append. and client version
     * on core level read platform level and append. if the value userAgent is null then we pass platform as default.
     * sample output
     * android core-client/0.0.19/ app-version/11.40.0
     * @param platform where the sdk is being used [Platform.Type.ANDROID] or [Platform.Type.IOS] or [Platform.Type.JVM] or [Platform.Type.BROWSER]
     * @param clientSDKName the sdk making api calls name
     * @param clientSDKVersion the sdk making api version
     * @param appVersion version of application where the sdk is being used
     */
    fun getUserAgent(
        platform: Platform,
        clientSDKName: String,
        clientSDKVersion: String?,
        appName: String?,
        appVersion: String?,
    ): String {
        val userAgent = StringBuilder()
        userAgent.append("${appName ?: "NA"}/${appVersion ?: "NA"}")
        userAgent.append(" ")
        userAgent.append("$clientSDKName/${clientSDKVersion ?: "NA"}")
        userAgent.append(" ")
        userAgent.append(platform.userAgent)
        return userAgent.toString()
    }

    /**
     * @return a [URLProtocol] based on the given protocol
     * @param protocol passed protocol as String.
     * This will return https as default given null or empty or illegal protocol
     */
    fun protocol(protocol: String?): URLProtocol {
        val urlProtocol = URLProtocol.createOrDefault(protocol ?: URLProtocol.HTTPS.name)
        return if (urlProtocol.defaultPort == DEFAULT_PORT) {
            URLProtocol.HTTPS
        } else {
            urlProtocol
        }
    }

    /**
     * Constructs a ClientException based on the HttpResponse
     * @param message optional message to attach to Exception
     * @param cause optional cause as [Throwable] to attach to Exception
     * if message is not set, it will set the Http response as message.
     * @return a [ClientException] with values filled
     */
    suspend fun HttpResponse.asClientException(
        message: String = "",
        cause: Throwable? = null
    ): ClientException {
        return try {
            val body = this.bodyAsText()
            val errorBody = getErrorBody(body)
            val errorMessage = message.ifEmpty { errorBody.message?:this.toString() }
            ClientException(
                httpStatusCode = this.status.value,
                errorBody = getErrorBody(body),
                message = errorMessage,
                errorType = "HTTP",
                cause = cause,
                errorString = body,
                httpURl = this.request.url.toString()
            )
        } catch (e: Throwable) {
            ClientException.listener(e.asClientException())
            return ClientException(
                cause = e,
                message = "Failed to convert $this into a ClientException.",
                errorType = "HTTP",
                httpURl = this.request.url.toString()

            )
        }
    }

    /**
     * similar to the HttpResponse.asClientException only that it covers
     * the ResponseException thrown by Ktor.
     *
     * asClientException is an extension function for the ResponseException class,
     * it converts a ResponseException object into a ClientException object.
     *
     * @param message : a string that contains a custom error message, it's optional
     * @param cause : a Throwable object that represents the cause of the error, it's optional
     *
     * @return : a ClientException object that represents the converted error
     */
    suspend fun ResponseException.asClientException(
        message: String = "",
        cause: Throwable? = null
    ): ClientException {
        return try {
            // Getting the response body as text
            val body = this.response.bodyAsText()
            // Extracting the error body from the response
            val errorBody = getErrorBody(body)
            // Setting the error message, it's either the extracted error message or the custom message or the default string representation of the exception
            val errorMessage = message.ifEmpty { errorBody.message?:this.message }
            // Creating the ClientException object
            ClientException(
                httpStatusCode = this.response.status.value, // setting the http status code
                errorBody = errorBody, // setting the error body
                message = errorMessage, // setting the error message
                code = errorBody.code, // setting the error code
                errorType = "HTTP", // setting the error type
                cause = cause?:this, // setting the cause of the error
                errorString = body, // setting the error string,
                httpURl = this.response.request.url.toString()
            )
        } catch (e: Throwable) {
            // Logging the exception
            ClientException.listener(e.asClientException())
            // Throwing a new ClientException object with a custom message that indicates the failure.
            throw ClientException(
                cause = e,
                message = "Failed to convert $this into a ClientException.",
                errorType = "HTTP",
                httpURl = this.response.request.url.toString()
            )
        }
    }

    /**
     * Returns a Boolean indicating whether the [HttpResponse] has the specified [status] code.
     * Reusable, as we can reuse the hasStatus function to check for multiple status codes. rather than creating
     * a single method for each status check e.g isOK,isAccepted,is.....
     * @param status The [HttpStatusCode] to check against the [HttpResponse] status.
     * @return True if the [HttpResponse] has the specified [status] code, false otherwise.
     */
    fun HttpResponse.hasStatus(status: HttpStatusCode) = (this.status == status)

    /**
     * Returns a [Boolean] indicating whether the [HttpResponse] is successful.
     * @return True if the [HttpResponse] has [HttpStatusCode] value in the range of 200-299 , false otherwise.
     */
    fun HttpResponse.successful() = (this.status.value in 200..299)

}