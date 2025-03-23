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

import com.russhwolf.settings.ExperimentalSettingsApi
import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.Utils.launchPeriodicAsync
import io.telereso.kmp.core.models.ExpirableValue
import io.telereso.kmp.core.models.fromJson
import io.telereso.kmp.core.models.toJson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlin.time.Duration
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getBooleanOrNullFlow
import com.russhwolf.settings.coroutines.getDoubleFlow
import com.russhwolf.settings.coroutines.getDoubleOrNullFlow
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getFloatOrNullFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getIntOrNullFlow
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow

/**
 * A Wrapper for saving simple key-value data.
 * Currently it only supports saving of primitive data types.
 * TODO later we can add coroutines support or settings listeners  since our implementation can support it.
 * TODO later we can support wrapped using a property delegate.
 *
 */
interface Settings {

    companion object {
        internal var reactNativeSettings: ((Duration?) -> Settings)? = null
        internal var weChatSettings: ((Duration?) -> Settings)? = null

        fun get(clearExpiredKeysDuration: Duration? = null): Settings =
            reactNativeSettings?.invoke(clearExpiredKeysDuration)
                ?: weChatSettings?.invoke(clearExpiredKeysDuration)
                ?: SettingsImpl(clearExpiredKeysDuration = clearExpiredKeysDuration)

        fun getInMemory(clearExpiredKeysDuration: Duration? = null): Settings =
            InMemorySetting(clearExpiredKeysDuration = clearExpiredKeysDuration)
    }

    var listener : Settings.Listener?

    /**
     * Returns a `Set` containing all the keys present in this [Settings].
     */
    val keys: Set<String>

    /**
     * Returns the number of key-value pairs present in this [Settings].
     */
    val size: Int

    /**
     * Clears all values stored in this [Settings] instance.
     */
    fun clear()

    /**
     * Will loop all keys and remove the expired ones
     */
    fun removeExpiredKeys()

    /**
     * In case you need to stop removing expired keys
     */
    fun cancelRemovingExpiredKeys()

    /**
     * Removes the value stored at [key].
     */
    fun remove(key: String)

    /**
     * Returns `true` if there is a value stored at [key], or `false` otherwise.
     */
    fun hasKey(key: String): Boolean

    /**
     * Stores the `Int` [value] at [key].
     */
    fun putInt(key: String, value: Int)

    /**
     * Returns the `Int` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getInt(key: String, defaultValue: Int): Int

    /**
     * Returns the `Int` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getIntOrNull(key: String): Int?

    /**
     * Returns a Flow  `Int` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getIntFlow(key: String, defaultValue: Int): Flow<Int>

    /**
     * Returns a Flow `Int` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getIntFlow(key: String): Flow<Int?>

    /**
     * Stores the `Long` [value] at [key].
     */
    fun putLong(key: String, value: Long)

    /**
     * Returns the `Long` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getLong(key: String, defaultValue: Long): Long

    /**
     * Returns the `Long` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getLongOrNull(key: String): Long?

    /**
     * Returns a Flow `Long` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getLongFlow(key: String): Flow<Long?>

    /**
     * Returns a Flow `Long` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getLongFlow(key: String, defaultValue: Long): Flow<Long>

    /**
     * Stores the `String` [value] at [key].
     */
    fun putString(key: String, value: String)

    /**
     * Returns the `String` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getString(key: String, defaultValue: String): String

    /**
     * Returns the `String` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getStringOrNull(key: String): String?

    /**
     * Returns a Flow `String` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getStringFlow(key: String, defaultValue: String): Flow<String>

    /**
     * Returns a Flow `String` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getStringFlow(key: String): Flow<String?>

    /**
     * Stores the `Float` [value] at [key].
     */
    fun putFloat(key: String, value: Float)

    /**
     * Returns the `Float` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getFloat(key: String, defaultValue: Float): Float

    /**
     * Returns the `Float` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getFloatOrNull(key: String): Float?

    /**
     * Returns a Flow `Float` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getFloatFlow(key: String, defaultValue: Float): Flow<Float>

    /**
     * Returns a Flow `Float` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getFloatFlow(key: String): Flow<Float?>

    /**
     * Stores the `Double` [value] at [key].
     */
    fun putDouble(key: String, value: Double)

    /**
     * Returns the `Double` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getDouble(key: String, defaultValue: Double): Double

    /**
     * Returns the `Double` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getDoubleOrNull(key: String): Double?

    /**
     * Returns a Flow `Double` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double>

    /**
     * Returns a Flow `Double` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getDoubleFlow(key: String): Flow<Double?>

    /**
     * Stores the `Boolean` [value] at [key].
     */
    fun putBoolean(key: String, value: Boolean)

    /**
     * Returns the `Boolean` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    /**
     * Returns the `Boolean` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getBooleanOrNull(key: String): Boolean?

    /**
     * Returns a Flow `Boolean` value stored at [key], or [defaultValue] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean>

    /**
     * Returns a Flow `Boolean` value stored at [key], or `null` if no value was stored. If a value of a different type was
     * stored at `key`, the behavior is not defined.
     */
    fun getBooleanFlow(key: String): Flow<Boolean?>

    fun putExpirableString(key: String, value: String, exp: Long)

    fun putExpirableString(key: String, value: String, exp: Duration){
        putExpirableString(key, value, Clock.System.now().epochSeconds + exp.inWholeSeconds)
    }

    fun getExpirableString(key: String, default: String?): String?

    fun getExpirableString(key: String): String?

    interface Listener {
        /**
         * Unsubscribes this [Listener] from receiving updates to the value at the key it monitors. After calling
         * this method you should no longer hold a reference to the listener.
         */
        fun deactivate()
        fun onRemoveExpiredKeys()
    }
}

/**
 * A handle to a listener instance returned by one of the addListener methods of [ObservableSettings], so it can be
 * deactivated as needed.
 */


/**
 * https://github.com/russhwolf/multiplatform-settings
 */
@OptIn(ExperimentalSettingsApi::class)
internal class SettingsImpl(
    private val settings : com.russhwolf.settings.Settings = com.russhwolf.settings.Settings(),
    clearExpiredKeysDuration: Duration? = null
) : Settings {

    private val settingsObservable: ObservableSettings get() = settings as ObservableSettings

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

    override val keys: Set<String>
        get() = settings.keys
    override val size: Int
        get() = settings.size

    override fun clear() {
        /**
         * Note that for the NSUserDefaultsSettings implementation, some entries are un-removable and therefore may still be present after a clear() call.
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
        return settingsObservable.getIntFlow(key, defaultValue)
    }

    override  fun getIntFlow(key: String): Flow<Int?> {
        return settingsObservable.getIntOrNullFlow(key)
    }

    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return settingsObservable.getStringFlow(key, defaultValue)
    }

    override fun getStringFlow(key: String): Flow<String?> {
        return settingsObservable.getStringOrNullFlow(key)
    }

    override fun getLongFlow(key: String): Flow<Long?> {
        return settingsObservable.getLongOrNullFlow(key)
    }

    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return settingsObservable.getLongFlow(key, defaultValue)
    }

    override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return settingsObservable.getFloatFlow(key, defaultValue)
    }

    override fun getFloatFlow(key: String): Flow<Float?> {
        return settingsObservable.getFloatOrNullFlow(key)
    }

    override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> {
        return settingsObservable.getDoubleFlow(key, defaultValue)
    }

    override fun getDoubleFlow(key: String): Flow<Double?> {
        return settingsObservable.getDoubleOrNullFlow(key)
    }

    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return settingsObservable.getBooleanFlow(key, defaultValue)
    }

    override fun getBooleanFlow(key: String): Flow<Boolean?> {
        return settingsObservable.getBooleanOrNullFlow(key)
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