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

import io.telereso.kmp.core.ContextScope
import io.telereso.kmp.core.DispatchersProvider
import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.Settings
import io.telereso.kmp.core.Utils
import io.telereso.kmp.core.Utils.launchPeriodicAsync
import io.telereso.kmp.core.models.ExpirableValue
import io.telereso.kmp.core.models.fromJson
import io.telereso.kmp.core.models.toJson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.time.Duration

open class ReactNativeSettings(
    clearExpiredKeysDuration: Duration? = null
) : Settings {

    companion object {
        private val flow = MutableStateFlow(mutableMapOf<String, String?>())
        suspend fun setup(): Int {
            val map = mutableMapOf<String, String?>()
            if (AsyncStorage != null) {
                val keys = (AsyncStorage.getAllKeys() as Promise<Array<String>>).await()
                keys.forEach { key ->
                    val value = (AsyncStorage.getItem(key) as Promise<String?>).await()
                    map[key] = value
                }
                flow.value = map
            } else {
                console.warn("⚠️ AsyncStorage is unavailable.")
            }
            return flow.value.size
        }
    }

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
        get() = flow.value.keys

    override val size: Int
        get() = flow.value.size

    override fun clear() {
        AsyncStorage?.clear()
        flow.value = mutableMapOf()
    }

    override fun removeExpiredKeys() {
        logDebug("RemoveExpiredKeys - size: $size")
        keys.forEach {
            getExpirableString(it)
        }
        listener?.onRemoveExpiredKeys()
    }

    override fun cancelRemovingExpiredKeys() {
        removeExpiredJob?.cancel()
        removeExpiredJob = null
    }

    override fun remove(key: String) {
        AsyncStorage?.removeItem(key)
        flow.update { it.toMutableMap().apply { remove(key) } }
    }

    override fun hasKey(key: String): Boolean {
        return getStringOrNull(key) != null
    }

    override fun putInt(key: String, value: Int) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getString(key, defaultValue.toString()).toInt()
    }

    override fun getIntOrNull(key: String): Int? {
        return getStringOrNull(key)?.toIntOrNull()
    }

    override fun putLong(key: String, value: Long) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getString(key, defaultValue.toString()).toLong()
    }

    override fun getLongOrNull(key: String): Long? {
        return getStringOrNull(key)?.toLongOrNull()
    }

    override fun putString(key: String, value: String) {
        AsyncStorage?.setItem(key, value)
        flow.update { it.toMutableMap().apply { put(key, value) } }
    }

    override fun getString(key: String, defaultValue: String): String {
        return getStringOrNull(key) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return flow.value[key]
    }

    override fun putFloat(key: String, value: Float) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getString(key, defaultValue.toString()).toFloat()
    }

    override fun getFloatOrNull(key: String): Float? {
        return getStringOrNull(key)?.toFloatOrNull()
    }

    override fun putDouble(key: String, value: Double) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getString(key, defaultValue.toString()).toDouble()
    }

    override fun getDoubleOrNull(key: String): Double? {
        return getStringOrNull(key)?.toDoubleOrNull()
    }

    override fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key, defaultValue.toString()).toBoolean()
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return getStringOrNull(key)?.toBoolean()
    }

    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return flow.map { it[key]?.toIntOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getIntFlow(key: String): Flow<Int?> {
        return flow.map { it[key]?.toIntOrNull() }.distinctUntilChanged()
    }

    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return flow.map { it[key] ?: defaultValue }.distinctUntilChanged()
    }

    override fun getStringFlow(key: String): Flow<String?> {
        return flow.map { it[key] }.distinctUntilChanged()
    }

    override fun getLongFlow(key: String): Flow<Long?> {
        return flow.map { it[key]?.toLongOrNull() }.distinctUntilChanged()
    }

    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return flow.map { it[key]?.toLongOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return flow.map { it[key]?.toFloatOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getFloatFlow(key: String): Flow<Float?> {
        return flow.map { it[key]?.toFloatOrNull() }.distinctUntilChanged()
    }

    override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> {
        return flow.map { it[key]?.toDoubleOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getDoubleFlow(key: String): Flow<Double?> {
        return flow.map { it[key]?.toDoubleOrNull() }.distinctUntilChanged()
    }

    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return flow.map { it[key]?.toBoolean() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getBooleanFlow(key: String): Flow<Boolean?> {
        return flow.map { it[key]?.toBoolean() }.distinctUntilChanged()
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

private var AsyncStorage: dynamic = null

@OptIn(DelicateCoroutinesApi::class)
@JsExport
fun setupReactNativeStorage(asyncStorage: dynamic): Promise<Int> {
    Settings.reactNativeSettings = { ReactNativeSettings(it) }
    AsyncStorage = asyncStorage
    return GlobalScope.promise {
        ReactNativeSettings.setup()
    }
}
