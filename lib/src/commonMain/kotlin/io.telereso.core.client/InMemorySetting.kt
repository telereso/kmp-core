package io.telereso.core.client

/**
 * class provides an in memory verison of the settings, mostly useful during unit tests.
 */
class InMemorySetting : Settings {

    private val settings = MapSettings()

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