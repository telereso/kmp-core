package io.telereso.kmp.core

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
class MapSettings constructor(private val delegate: MutableMap<String, Any> = mutableMapOf()) :
    ObservableSettings {
    private val listeners = mutableListOf<() -> Any>()
    private fun invokeListeners() = listeners.forEach { it() }

    constructor(vararg items: Pair<String, Any>) : this(mutableMapOf(*items))

    override val keys: Set<String> get() = delegate.keys
    override val size: Int get() = delegate.size

    override fun clear() {
        delegate.clear()
        invokeListeners()
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

    override fun addIntListener(
        key: String,
        defaultValue: Int,
        callback: (Int) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getInt(key, defaultValue)) }

    override fun addLongListener(
        key: String,
        defaultValue: Long,
        callback: (Long) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getLong(key, defaultValue)) }

    override fun addStringListener(
        key: String,
        defaultValue: String,
        callback: (String) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getString(key, defaultValue)) }

    override fun addFloatListener(
        key: String,
        defaultValue: Float,
        callback: (Float) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getFloat(key, defaultValue)) }

    override fun addDoubleListener(
        key: String,
        defaultValue: Double,
        callback: (Double) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getDouble(key, defaultValue)) }

    override fun addBooleanListener(
        key: String,
        defaultValue: Boolean,
        callback: (Boolean) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getBoolean(key, defaultValue)) }

    override fun addIntOrNullListener(
        key: String,
        callback: (Int?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getIntOrNull(key)) }

    override fun addLongOrNullListener(
        key: String,
        callback: (Long?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getLongOrNull(key)) }

    override fun addStringOrNullListener(
        key: String,
        callback: (String?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getStringOrNull(key)) }

    override fun addFloatOrNullListener(
        key: String,
        callback: (Float?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getFloatOrNull(key)) }

    override fun addDoubleOrNullListener(
        key: String,
        callback: (Double?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getDoubleOrNull(key)) }

    override fun addBooleanOrNullListener(
        key: String,
        callback: (Boolean?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getBooleanOrNull(key)) }

    private fun addListener(key: String, callback: () -> Unit): SettingsListener {
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
    ) : SettingsListener {
        override fun deactivate() {
            listeners -= listener
        }
    }
}
