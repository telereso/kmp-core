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

import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.Utils.launchPeriodicAsync
import io.telereso.kmp.core.models.ExpirableValue
import io.telereso.kmp.core.models.fromJson
import io.telereso.kmp.core.models.toJson
import kotlinx.coroutines.Deferred
import kotlin.time.Duration

/**
 * A collection of storage-backed key-value data
 *
 * This class allows storage of values with the [Int], [Long], [String], [Float], [Double], or [Boolean] types, using a
 * [String] reference as a key.
 *
 * The `CommonMapSettings` implementation is intended for use in unit tests. It differs from production implementations
 * because the state exists only in-memory and has no mechanism for persistence.
 *
 * This class can be instantiated by wrapping a [MutableMap] or set of [Pair] entries.
 *
 * This implementation is verified against the same test suite as the real platform-specific implementations to ensure
 * it shares the same behavior, assuming the default [mutableMapOf] delegate is used.
 */
class MapSettings constructor(
    private val delegate: MutableMap<String, Any> = mutableMapOf(),
    clearExpiredKeysDuration: Duration? = null
) :
    ObservableSettings {
    private val listeners = mutableListOf<() -> Any>()
    private fun invokeListeners() = listeners.forEach { it() }

    constructor(vararg items: Pair<String, Any>) : this(mutableMapOf(*items))

    override var listener: Settings.Listener? = null
    private var removeExpiredJob : Deferred<Unit>? = null
    
    init {
        clearExpiredKeysDuration?.let {
            removeExpiredJob = ContextScope.get(DispatchersProvider.Default)
                .launchPeriodicAsync(it) {
                    removeExpiredKeys()
                }
        }
    }

    override val keys: Set<String> get() = delegate.keys
    override val size: Int get() = delegate.size

    override fun clear() {
        delegate.clear()
        invokeListeners()
    }

    override fun removeExpiredKeys() {
        logDebug("RemoveExpiredKeys - size: ${delegate.size}")
        delegate.keys.forEach {
            getExpirableString(it)
        }
        listener?.onRemoveExpiredKeys()
    }

    override fun cancelRemovingExpiredKeys() {
        removeExpiredJob?.cancel()
        removeExpiredJob = null
    }

    override fun remove(key: String) {
        delegate -= key
        invokeListeners()
    }

    override fun hasKey(key: String): Boolean = key in delegate

    override fun putInt(key: String, value: Int) {
        delegate[key] = value
        invokeListeners()
    }

    override fun getInt(key: String, defaultValue: Int): Int = delegate[key] as? Int ?: defaultValue

    override fun getIntOrNull(key: String): Int? = delegate[key] as? Int

    override fun putLong(key: String, value: Long) {
        delegate[key] = value
        invokeListeners()
    }

    override fun getLong(key: String, defaultValue: Long): Long =
        delegate[key] as? Long ?: defaultValue

    override fun getLongOrNull(key: String): Long? = delegate[key] as? Long

    override fun putString(key: String, value: String) {
        delegate[key] = value
        invokeListeners()
    }

    override fun getString(key: String, defaultValue: String): String =
        delegate[key] as? String ?: defaultValue

    override fun getStringOrNull(key: String): String? = delegate[key] as? String

    override fun putFloat(key: String, value: Float) {
        delegate[key] = value
        invokeListeners()
    }

    override fun getFloat(key: String, defaultValue: Float): Float =
        delegate[key] as? Float ?: defaultValue

    override fun getFloatOrNull(key: String): Float? = delegate[key] as? Float

    override fun putDouble(key: String, value: Double) {
        delegate[key] = value
        invokeListeners()
    }

    override fun getDouble(key: String, defaultValue: Double): Double =
        delegate[key] as? Double ?: defaultValue

    override fun getDoubleOrNull(key: String): Double? = delegate[key] as? Double

    override fun putBoolean(key: String, value: Boolean) {
        delegate[key] = value
        invokeListeners()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        delegate[key] as? Boolean ?: defaultValue

    override fun getBooleanOrNull(key: String): Boolean? = delegate[key] as? Boolean

    override fun putExpirableString(key: String, value: String, exp: Long) {
        putString(key, ExpirableValue(value, exp).toJson())
    }

    override fun getExpirableString(key: String, default: String?): String? {
        val v = getStringOrNull(key) ?: return default
        val ev = ExpirableValue.fromJson(v)
        if (Utils.isExpired(ev.exp)) {
            logDebug("Removing expired - $key")
            remove(key)
            return default
        }
        return ev.value
    }

    override fun getExpirableString(key: String): String? {
        return getExpirableString(key, null)
    }

    override fun addIntListener(
        key: String,
        defaultValue: Int,
        callback: (Int) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getInt(key, defaultValue)) }

    override fun addLongListener(
        key: String,
        defaultValue: Long,
        callback: (Long) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getLong(key, defaultValue)) }

    override fun addStringListener(
        key: String,
        defaultValue: String,
        callback: (String) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getString(key, defaultValue)) }

    override fun addFloatListener(
        key: String,
        defaultValue: Float,
        callback: (Float) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getFloat(key, defaultValue)) }

    override fun addDoubleListener(
        key: String,
        defaultValue: Double,
        callback: (Double) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getDouble(key, defaultValue)) }

    override fun addBooleanListener(
        key: String,
        defaultValue: Boolean,
        callback: (Boolean) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getBoolean(key, defaultValue)) }

    override fun addIntOrNullListener(
        key: String,
        callback: (Int?) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getIntOrNull(key)) }

    override fun addLongOrNullListener(
        key: String,
        callback: (Long?) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getLongOrNull(key)) }

    override fun addStringOrNullListener(
        key: String,
        callback: (String?) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getStringOrNull(key)) }

    override fun addFloatOrNullListener(
        key: String,
        callback: (Float?) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getFloatOrNull(key)) }

    override fun addDoubleOrNullListener(
        key: String,
        callback: (Double?) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getDoubleOrNull(key)) }

    override fun addBooleanOrNullListener(
        key: String,
        callback: (Boolean?) -> Unit
    ): Settings.Listener =
        addListener(key) { callback(getBooleanOrNull(key)) }

    private fun addListener(key: String, callback: () -> Unit): Settings.Listener {
        var prev = delegate[key]

        val listener = {
            val current = delegate[key]
            if (prev != current) {
                callback()
                prev = current
            }
        }
        listeners += listener
        return Listener(listeners, listener)
    }

    /**
     * A handle to a listener instance returned by one of the addListener methods of [ObservableSettings], so it can be
     * deactivated as needed.
     *
     * In the [CommonMapSettings] implementation this simply wraps a lambda parameter which is being called whenever a
     * mutating API is called. Unlike platform implementations, this listener will NOT be called if the underlying map
     * is mutated by something other than the `MapSettings` instance that originally created the listener.
     */
    class Listener internal constructor(
        private val listeners: MutableList<() -> Any>,
        private val listener: () -> Unit
    ) : Settings.Listener {
        override fun deactivate() {
            listeners -= listener
        }

        override fun onRemoveExpiredKeys() {

        }
    }
}
