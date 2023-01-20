package io.telereso.core.client

import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * @suppress
 */
object Utils {

    val jsonSerializer = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    val jsonPrettySerializer = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Caution: this is only intented for UNit Testing.
     * it allows us to time travel so access tokens not expire during running unit tests.
     */
    var unitTestInstance: Instant? = null

    private fun getJsonBody(encodeJwt: String): String {
        return try {
            val split = encodeJwt.split(".").toTypedArray()
            split[1].decodeBase64String()
        } catch (e: Throwable) {
            ClientException.listener(e.toClientException())
            ""
        }
    }

    /**
     * @param tokenBufferTime In seconds. e.g 300 is 5 minutes in sec
     */
    fun hasJwtExpired(token: String, tokenBufferTime: Int): Boolean {
        val expiryDate = getExpiryTime(token) - tokenBufferTime
        if (expiryDate <= 0L) return true
        val currentDate = (unitTestInstance ?: Clock.System.now()).toEpochMilliseconds() / 1000
        return currentDate >= expiryDate
    }

    /**
     * getExpiryTime - a function that takes a String response and returns a Long expiry time
     *
     * @param response: String - a String response
     * @return Long - expiry time
     */
    @Throws(NullPointerException::class)
    private fun getExpiryTime(response: String): Long {
        // Get the JSON body from the response
        val jsonBody = getJsonBody(response)

        // Attempt to decode the JSON body into an object of type DecodeResponse
        return try {
            //Decode the response from json string to object
            val decodedResponse = Json.decodeFromString<JwtPayload>(jsonBody)
            //return expiry time if exist, otherwise throw
            decodedResponse.exp ?: throw NullPointerException("expire time cannot be null.").toClientException()
        } catch (e: Throwable) {
            // Call the listener function to handle the exception
            ClientException.listener(e.toClientException())
            // Re-throw the exception
            throw e.toClientException()
        }
    }


    /**
     * Retrieves and decodes a response from a token
     *
     * @param fromToken The token used for authentication or authorization
     *
     * @return The decoded response
     *
     * @throws Throwable if an exception is thrown during decoding
     */
    @Throws(Throwable::class)
    fun getDecodedResponse(fromToken: String): JwtPayload {
        //retrieve the JSON body of the response by calling getJsonBody() function
        val jsonBody = getJsonBody(fromToken)
        //attempt to decode the JSON body and return the decoded response
        return try {
            Json.decodeFromString(jsonBody)
        } catch (e: Throwable) {
            //if an exception is thrown during decoding, call the ClientException.listener method
            ClientException.listener(e.toClientException())
            //re-throw the exception
            throw e
        }
    }
}