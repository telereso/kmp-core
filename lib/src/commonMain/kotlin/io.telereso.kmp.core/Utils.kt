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

import io.ktor.util.*
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.JwtPayload
import io.telereso.kmp.core.models.asClientException
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
            ClientException.listener(e.asClientException())
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
     * Check if provided epoch seconds are already expired
     * @param exp epoch seconds to be checked if already passed or still in the future
     */
    fun isExpired(exp:Long): Boolean{
        if (exp <= 0L) return true
        val currentDate = (unitTestInstance ?: Clock.System.now()).toEpochMilliseconds() / 1000
        return currentDate >= exp
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
            val decodedResponse =  Http.ktorConfigJson.decodeFromString<JwtPayload>(jsonBody)
            //return expiry time if exist, otherwise throw
            decodedResponse.exp ?: throw NullPointerException("expire time cannot be null.").asClientException()
        } catch (e: Throwable) {
            // Call the listener function to handle the exception
            ClientException.listener(e.asClientException())
            // Re-throw the exception
            throw e.asClientException()
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
    fun getDecodedResponse(fromToken: String): Map<String,String> {
        //retrieve the JSON body of the response by calling getJsonBody() function
        val jsonBody = getJsonBody(fromToken)
        //attempt to decode the JSON body and return the decoded response
        return try {
            Http.ktorConfigJson.decodeFromString(jsonBody)
        } catch (e: Throwable) {
            //if an exception is thrown during decoding, call the ClientException.listener method
            ClientException.listener(e.asClientException())
            //re-throw the exception
            throw e
        }
    }
}