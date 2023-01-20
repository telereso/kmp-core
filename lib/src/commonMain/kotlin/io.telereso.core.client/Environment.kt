package io.telereso.core.client

import kotlin.js.JsExport

/**
 * Defines the different Environments the sdk support.
 * this is usually passed to the SDK via the [Config] builder.
 */
@JsExport
enum class Environment {
    /**
     * staging environment value
     */
    STAGING,
    /**
     * production environment value
     */
    PRODUCTION,
}