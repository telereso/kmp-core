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
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * class provides an in memory version of the settings, mostly useful during unit tests.
 */
class InMemorySetting(
    clearExpiredKeysDuration: Duration? = null
) : Settings {

    private val settings = MapSettings()

    override var listener: Settings.Listener? = null
    private var removeExpiredJob: Deferred<Unit>? = null

    init {
        clearExpiredKeysDuration?.let {
            removeExpiredJob = ContextScope.get(DispatchersProvider.Default)
                .launchPeriodicAsync(it) {
                    removeExpiredKeys()
                }
        }
    }

    override val keys: Set<String>
        get() = settings.keys
    override val size: Int
        get() = settings.size

    override fun clear() {
        /**
         * Note that for the NSUserDefaultsSettings implementation, some entries are unremovable and therefore may still be present after a clear() call.
         * Thus, size is not generally guaranteed to be zero after a clear().
         * here lets try on iOS or we should always set all values to null before we clear them?
         */
        settings.clear()
    }

    override fun removeExpiredKeys() {
        logDebug("RemoveExpiredKeys - size: ${settings.size}")
        settings.keys.forEach {
            getExpirableString(it)
        }
        listener?.onRemoveExpiredKeys()
    }

    override fun cancelRemovingExpiredKeys() {
        removeExpiredJob?.cancel()
        removeExpiredJob = null
    }

    override fun remove(key: String) {
        settings.remove(key)
    }

    override fun hasKey(key: String): Boolean {
        return settings.hasKey(key)
    }

    override fun putInt(key: String, value: Int) {
        settings.putInt(key, value)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return settings.getInt(key, defaultValue)
    }

    override fun getIntOrNull(key: String): Int? {
        return settings.getIntOrNull(key)
    }

    override fun putLong(key: String, value: Long) {
        settings.putLong(key, value)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return settings.getLong(key, defaultValue)
    }

    override fun getLongOrNull(key: String): Long? {
        return settings.getLongOrNull(key)
    }

    override fun putString(key: String, value: String) {
        settings.putString(key, value)
    }

    override fun getString(key: String, defaultValue: String): String {
        return settings.getString(key, defaultValue)
    }

    override fun getStringOrNull(key: String): String? {
        return settings.getStringOrNull(key)
    }

    override fun putFloat(key: String, value: Float) {
        settings.putFloat(key, value)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return settings.getFloat(key, defaultValue)
    }

    override fun getFloatOrNull(key: String): Float? {
        return settings.getFloatOrNull(key)
    }

    override fun putDouble(key: String, value: Double) {
        settings.putDouble(key, value)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return settings.getDouble(key, defaultValue)
    }

    override fun getDoubleOrNull(key: String): Double? {
        return settings.getDoubleOrNull(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return settings.getBooleanOrNull(key)
    }

    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return settings.getIntFlow(key, defaultValue)
    }

    override fun getIntFlow(key: String): Flow<Int?> {
        return settings.getIntFlow(key)
    }

    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return settings.getStringFlow(key, defaultValue)
    }

    override fun getStringFlow(key: String): Flow<String?> {
        return settings.getStringFlow(key)
    }

    override fun getLongFlow(key: String): Flow<Long?> {
        return settings.getLongFlow(key)
    }

    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return settings.getLongFlow(key, defaultValue)
    }

    override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return settings.getFloatFlow(key, defaultValue)
    }

    override fun getFloatFlow(key: String): Flow<Float?> {
        return settings.getFloatFlow(key)
    }

    override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> {
        return settings.getDoubleFlow(key, defaultValue)
    }

    override fun getDoubleFlow(key: String): Flow<Double?> {
        return settings.getDoubleFlow(key)
    }

    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return settings.getBooleanFlow(key, defaultValue)
    }

    override fun getBooleanFlow(key: String): Flow<Boolean?> {
        return settings.getBooleanFlow(key)
    }

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
}