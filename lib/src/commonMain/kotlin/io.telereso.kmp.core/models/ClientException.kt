package io.telereso.kmp.core.models

import io.telereso.kmp.core.Http
import io.telereso.kmp.core.ThrowableSerializer
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
@JsExport
open class ClientException(
    override val message: String? = null,
    @Serializable(with = ThrowableSerializer::class)
    override val cause: Throwable? = null,
    val httpStatusCode: Int? = null,
    val errorBody: ErrorBody? = null,
    val errorString: String? = null,
    val errorType: String? = null,
    val httpURl: String? = null,
) : Throwable() {
    @JsName("ClientExceptionConstructor")
    constructor(cause: Throwable? = null) : this(null, cause = cause)

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
fun Throwable.asClientException(): ClientException {
    return if (this as? ClientException != null) this else ClientException(
        cause = this,
        message = this.message,
        errorType = "Throwable"
    )
}

@Deprecated(
    "Use asClientException() instead of toClientException ()",
    replaceWith = ReplaceWith("asClientException()", "io.telereso.kmp.core.models")
)
fun Throwable.toClientException(): ClientException {
    return asClientException()
}



