package io.telereso.kmp.core

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
    val clientId: String? = null,
    val userId: String? = null,
    val type: String? = null,
    val sessionId: String? = null
)
