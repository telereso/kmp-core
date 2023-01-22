package io.telereso.kmp.core

import io.github.aakira.napier.Napier


object Log {

    fun Any?.logDebug(message: String, throwable: Throwable? = null) {
        Log.d(this, message, throwable)
    }

    fun Any?.logInfo(message: String, throwable: Throwable? = null) {
        Log.i(this, message, throwable)
    }

    fun Any?.logError(throwable: Throwable? = null, message: String? = "") {
        Log.e(this, throwable, message ?: throwable?.message ?: "")
    }

    fun d(caller: Any?, message: String, throwable: Throwable? = null) {
        Napier.d(message, throwable, caller?.let { it::class.simpleName })
    }

    fun i(caller: Any?, message: String, throwable: Throwable? = null) {
        Napier.i(message, throwable, caller?.let { it::class.simpleName })
    }

    fun e(caller: Any?, throwable: Throwable? = null, message: String? = "") {
        Napier.e(
            message ?: throwable?.message ?: "",
            throwable,
            caller?.let { it::class.simpleName })
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Napier.d(message, throwable, tag)
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Napier.i(message, throwable, tag)
    }

    fun e(tag: String, throwable: Throwable? = null, message: String? = "") {
        Napier.e(message ?: throwable?.message ?: "", throwable, tag)
    }
}


