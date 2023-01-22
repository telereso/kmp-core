package io.telereso.kmp.core

/**
 * A handle to a listener instance returned by one of the addListener methods of ObservableSettings, so it can be deactivated as needed.
 */
interface ObservableSettings : Settings {

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addIntListener(
        key: String,
        defaultValue: Int,
        callback: (Int) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addLongListener(
        key: String,
        defaultValue: Long,
        callback: (Long) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addStringListener(
        key: String,
        defaultValue: String,
        callback: (String) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addFloatListener(
        key: String,
        defaultValue: Float,
        callback: (Float) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addDoubleListener(
        key: String,
        defaultValue: Double,
        callback: (Double) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addBooleanListener(
        key: String,
        defaultValue: Boolean,
        callback: (Boolean) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addIntOrNullListener(
        key: String,
        callback: (Int?) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addLongOrNullListener(
        key: String,
        callback: (Long?) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addStringOrNullListener(
        key: String,
        callback: (String?) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addFloatOrNullListener(
        key: String,
        callback: (Float?) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addDoubleOrNullListener(
        key: String,
        callback: (Double?) -> Unit
    ): SettingsListener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addBooleanOrNullListener(
        key: String,
        callback: (Boolean?) -> Unit
    ): SettingsListener
}