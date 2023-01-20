package io.telereso.core.client

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
     * To abstract the Ktor Specific Exception and provide our own general [ClientException] that
     * all platforms should consume.
     * It handles the Exceptions thrown during Ktor request.
     * This is mostly setup during httpClient building
     *             HttpResponseValidator {
    getHttpResponseValidator(this)
    }
     * @param config an HttpCallValidator config
     * @return a HttpResponseValidator that when an exception is of Ktor Client or Server it will throw a [ClientException] instead for any API call accross the SDK.
     *
     */
    fun getHttpResponseValidator(config: HttpCallValidator.Config) =
        config.handleResponseExceptionWithRequest { exception, _ ->
            when (exception) {
                is ClientRequestException -> {
                    throw exception.asClientException()
                }
                is ServerResponseException -> {
                    throw exception.asClientException()
                }
                else -> {
                    // this should no normally happen since Ktor only handles Server and Client specific execptions.
                    throw exception.toClientException()
                }
            }
        }

    /**
     * To build a user agent that provide information where this sdk is being used,
     * on client level pass app-version. here we can read the client version.
     * on client sdk level pass client-name and append. and client version
     * on core level read platform level and append. if the value userAgent is null then we pass platform as default.
     * sample output
     * android core-client/0.0.19/ app-version/11.40.0
     * @param platform where the sdk is being used [Platform.TYPE.ANDROID] or [Platform.TYPE.IOS] or [Platform.TYPE.JVM] or [Platform.TYPE.JS]
     * @param clientSDKName the sdk making api calls name
     * @param clientSDKVersion the sdk making api version
     * @param appVersion version of application where the sdk is being used
     */
    fun getUserAgent(
        platform: Platform,
        clientSDKName: String,
        clientSDKVersion: String?,
        appVersion: String?,
    ): String {
        val userAgent = StringBuilder()
        userAgent.append("${platform.type.name.lowercase()}/${appVersion ?: "NA"}")
        userAgent.append(" ")
        userAgent.append("$clientSDKName/${clientSDKVersion ?: "NA"}")
        userAgent.append(" ")
        userAgent.append(platform.name)
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
        message: String? = null,
        cause: Throwable? = null
    ): ClientException {
        return try {
            val body = this.bodyAsText()
            val apiError by lazy {
                ApiErrorBody.fromJson(body)
            }
            ClientException(
                httpStatusCode = this.status.value.toString(),
                errorBody = body,
                message = message ?: apiError.message ?: this.toString(),
                errorType = ApiErrorBody.fromJson(body).code,
                cause = cause
            )
        } catch (e: Throwable) {
            ClientException.listener(e.toClientException())
            throw ClientException(
                cause = e,
                message = "Failed to convert $this into ClientException"
            )
        }
    }

    /**
     * similar to the HttpResponse.asClientException only that it covers
     * the ResponseException thrown by Ktor.
     */
    suspend fun ResponseException.asClientException(
        message: String? = null,
        cause: Throwable? = null,
        errorType: String? = null
    ): ClientException {
        return try {
            val body = this.response.bodyAsText()
            val apiError by lazy {
                ApiErrorBody.fromJson(body)
            }
            ClientException(
                httpStatusCode = this.response.status.value.toString(),
                errorBody = body,
                message = message ?: apiError.message ?: this.toString(),
                errorType = errorType ?: apiError.code,
                cause = cause
            )
        } catch (e: Throwable) {
            ClientException.listener(e.toClientException())
            throw ClientException(
                cause = e,
                message = "Failed to convert $this into ClientException"
            )
        }
    }
}