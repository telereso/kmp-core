package io.telereso.core.client

import kotlin.js.JsExport

/**
 * Defines the different Environments the sdk support.
 * this is usally passed to the SDK via the [Config] builder.
 */
@JsExport
enum class Environment {
    /**
     * staging enviromnent value
     */
    STAGING,
    /**
     * production enviromnent value
     */
    PRODUCTION,
}