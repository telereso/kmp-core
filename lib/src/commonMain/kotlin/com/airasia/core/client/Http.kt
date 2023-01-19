package com.airasia.core.client

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
        prettyPrint = true
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
     * android sso-client/0.0.19/ app-version/11.40.0
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
     * Generates headers based on the past params. checks if passed param is null or empty before its added to the list of headers.
     * The headers are mostly useful when making an API call.
     * @param origin The origin string mapped as Origin
     * @param apiKey The client apiKey mapped as X-Api-Key
     * @param client_id The client id mapped as X-Aa-Client-Id
     * @param accessToken The accessToken mapped as Authorization
     * @param contentType The content Type mapped as Content-Type we mostly use Json as type.
     * @return a paired list of headers.
     */
    fun getHeaders(
        origin: String? = null,
        apiKey: String? = null,
        client_id: String? = null,
        accessToken: String? = null,
        contentType: ContentType? = null
    ): MutableList<Pair<String, String>> {
        val headersList = arrayListOf<Pair<String, String>>()

        if (contentType != null) {
            headersList.add(Pair(HttpHeaders.ContentType, contentType.toString()))
        }

        if (!origin.isNullOrEmpty()) {
            headersList.add(Pair(HttpHeaders.Origin, origin))
        }

        if (!apiKey.isNullOrEmpty()) {
            headersList.add(Pair("X-Api-Key", apiKey))
        }
        if (!client_id.isNullOrEmpty()) {
            headersList.add(Pair("X-Aa-Client-Id", client_id))
        }
        if (!accessToken.isNullOrEmpty()) {
            headersList.add(Pair(HttpHeaders.Authorization, accessToken))
        }
        return headersList
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
            ClientException(
                httpStatusCode = this.status.value.toString(),
                errorBody = body,
                message = message.ifEmpty { this.toString() },
                errorType = getApiErrorCode(body),
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
        message: String = "",
        cause: Throwable? = null
    ): ClientException {
        return try {
            val body = this.response.bodyAsText()
            ClientException(
                httpStatusCode = this.response.status.value.toString(),
                errorBody = body,
                message = message.ifEmpty { this.toString() },
                errorType = getApiErrorCode(body),
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