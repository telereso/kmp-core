package io.telereso.kmp.core.models

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@kotlinx.serialization.Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class JwtPayload(
    val iss: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    val sub: String? = null,
)
