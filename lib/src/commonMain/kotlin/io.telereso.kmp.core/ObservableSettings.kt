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
    ): Settings.Listener

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
    ): Settings.Listener

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
    ): Settings.Listener

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
    ): Settings.Listener

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
    ): Settings.Listener

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
    ): Settings.Listener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addIntOrNullListener(
        key: String,
        callback: (Int?) -> Unit
    ): Settings.Listener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addLongOrNullListener(
        key: String,
        callback: (Long?) -> Unit
    ): Settings.Listener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addStringOrNullListener(
        key: String,
        callback: (String?) -> Unit
    ): Settings.Listener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addFloatOrNullListener(
        key: String,
        callback: (Float?) -> Unit
    ): Settings.Listener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addDoubleOrNullListener(
        key: String,
        callback: (Double?) -> Unit
    ): Settings.Listener

    /**
     * Adds a listener which will call the supplied [callback] anytime the value at [key] changes. A [ISettings]
     * reference is returned which can be used to halt callbacks by calling [deactivate()][ISettings.deactivate].
     * A strong reference should be held to the `CommonSettings` returned by this method in order to avoid it being
     * garbage-collected.
     */
    fun addBooleanOrNullListener(
        key: String,
        callback: (Boolean?) -> Unit
    ): Settings.Listener
}