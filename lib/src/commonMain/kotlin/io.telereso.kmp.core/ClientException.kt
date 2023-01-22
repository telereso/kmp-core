package io.telereso.kmp.core

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
@ExperimentalJsExport
@JsExport
class ClientException(
    val httpStatusCode: String? = null,
    val errorBody: String? = null,
    override val message: String? = null,
    override val cause: Throwable? = null,
    val errorType: String? = null,
) : Throwable() {
    @JsName("ClientExceptionConstructor")
    constructor(cause: Throwable? = null) : this(null,null,null,cause = cause)

    /**
     * class companion class
     */
    companion object {
        /**
         * Expose a global listener to use with third party analytics and crashlytics.
         */
        var listener: ((t: Throwable) -> Unit) = {  }
    }
}


/**
 * A data class used to generate a proper Api Error body, by default, each API will return a code and message on failure.
 * @param code The code that defines the error from API.
 * @param message The message about the error from API.
 */
@Serializable
data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null
) {

    fun toJson(): String {
        return Utils.jsonSerializer.encodeToString(serializer(), this)
    }

    fun toJsonPretty(): String {
        return Utils.jsonPrettySerializer.encodeToString(serializer(), this)
    }

    /**
     * companion
     */
    companion object {
        /**
         * static method to convert a given json into an [ApiErrorBody] objet
         * @param json the json to convert
         * @return an objet of [ApiErrorBody]
         */
        fun fromJson(json: String): ApiErrorBody {
            return Utils.jsonSerializer.decodeFromString(json)
        }

    }
}

/**
 * provides a safe casting of throwables
 * converts any throwable into a [ClientException].
 * Clients dont need to perform casting as its handled on the SDK level.
 * @return a [ClientException] object when casting succuess else it will return a [ClientException] with the throwable as a cause
 */
fun Throwable.toClientException(): ClientException {
    return if (this as? ClientException != null) this else ClientException(cause = this)
}

