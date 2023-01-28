package io.telereso.kmp.core.models


import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * A data class used to generate a proper Api Error body, by default, each API will return a code and message on failure.
 * @param code The code that defines the error from API.
 * @param message The message about the error from API.
 */
@Serializable
@JsExport
data class ErrorBody(
    val code:String? =null,
    val message:String? =null
)
