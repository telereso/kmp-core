package io.telereso.core.client

/**
 * A Wrapper for saving simple key-value data.
 * Currently it only supports saving of primitive data types.
 * TODO later we can add couritines support or settings listnerners  since our implmentation can support it.
 * TODO later we can support wrapped using a property delegate.
 *
 */
interface Settings {

    companion object {
        fun get(): Settings = SettingsImpl()
        fun getInMemory(): Settings = InMemorySetting()
    }

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
}

/**
 * A handle to a listener instance returned by one of the addListener methods of [ObservableSettings], so it can be
 * deactivated as needed.
 */
interface SettingsListener {
    /**
     * Unsubscribes this [SettingsListener] from receiving updates to the value at the key it monitors. After calling
     * this method you should no longer hold a reference to the listener.
     */
    fun deactivate()
}

/**
 * https://github.com/russhwolf/multiplatform-settings
 */
internal class SettingsImpl(val settings : com.russhwolf.settings.Settings = com.russhwolf.settings.Settings()) : Settings {

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
}