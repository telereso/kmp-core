package io.telereso.core.client

import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

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

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Model {
    /**
     * converts the given string to Json
     * @return a Json string
     */
    fun toJson(): String
    /**
     * converts the given string to a pretty Json
     * @return a pretty Json string
     */
    fun toJsonPretty(): String
}
