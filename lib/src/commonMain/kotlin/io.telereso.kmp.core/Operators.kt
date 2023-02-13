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

@Suppress("NOTHING_TO_INLINE")

/** Equivalent to [Settings.hasKey] */
inline operator fun Settings.contains(key: String): Boolean = hasKey(key)

/** Equivalent to [Settings.remove] */
inline operator fun Settings.minusAssign(key: String): Unit = remove(key)

/** Equivalent to [Settings.getInt] */
inline operator fun Settings.get(key: String, defaultValue: Int): Int =
    getInt(key, defaultValue)

/** Equivalent to [Settings.getLong] */
inline operator fun Settings.get(key: String, defaultValue: Long): Long =
    getLong(key, defaultValue)

/** Equivalent to [Settings.getString] */
inline operator fun Settings.get(key: String, defaultValue: String): String =
    getString(key, defaultValue)

/** Equivalent to [Settings.getFloat] */
inline operator fun Settings.get(key: String, defaultValue: Float): Float =
    getFloat(key, defaultValue)

/** Equivalent to [Settings.getDouble] */
inline operator fun Settings.get(key: String, defaultValue: Double): Double =
    getDouble(key, defaultValue)

/** Equivalent to [Settings.getBoolean] */
inline operator fun Settings.get(key: String, defaultValue: Boolean): Boolean =
    getBoolean(key, defaultValue)

/** Equivalent to [Settings.putInt] */
inline operator fun Settings.set(key: String, value: Int): Unit = putInt(key, value)

/** Equivalent to [Settings.putLong] */
inline operator fun Settings.set(key: String, value: Long): Unit = putLong(key, value)

/** Equivalent to [Settings.putString] */
inline operator fun Settings.set(key: String, value: String): Unit = putString(key, value)

/** Equivalent to [Settings.putFloat] */
inline operator fun Settings.set(key: String, value: Float): Unit = putFloat(key, value)

/** Equivalent to [Settings.putDouble] */
inline operator fun Settings.set(key: String, value: Double): Unit = putDouble(key, value)

/** Equivalent to [Settings.putBoolean] */
inline operator fun Settings.set(key: String, value: Boolean): Unit = putBoolean(key, value)

/**
 * Get the typed value stored at [key] if present, or return null if not. Throws [IllegalArgumentException] if [T] is
 * not one of `Int`, `Long`, `String`, `Float`, `Double`, or `Boolean`.
 */
inline operator fun <reified T : Any> Settings.get(key: String): T? = when (T::class) {
    Int::class -> getIntOrNull(key) as T?
    Long::class -> getLongOrNull(key) as T?
    String::class -> getStringOrNull(key) as T?
    Float::class -> getFloatOrNull(key) as T?
    Double::class -> getDoubleOrNull(key) as T?
    Boolean::class -> getBooleanOrNull(key) as T?
    else -> throw IllegalArgumentException("Invalid type!")
}

/**
 * Stores a typed value at [key], or remove what's there if [value] is null. Throws [IllegalArgumentException] if [T] is
 * not one of `Int`, `Long`, `String`, `Float`, `Double`, or `Boolean`.
 */
inline operator fun <reified T : Any> Settings.set(key: String, value: T?): Unit =
    if (value == null) {
        this -= key
    } else when (T::class) {
        Int::class -> putInt(key, value as Int)
        Long::class -> putLong(key, value as Long)
        String::class -> putString(key, value as String)
        Float::class -> putFloat(key, value as Float)
        Double::class -> putDouble(key, value as Double)
        Boolean::class -> putBoolean(key, value as Boolean)
        else -> throw IllegalArgumentException("Invalid type!")
    }

/** Equivalent to [Settings.remove] */
inline operator fun Settings.set(
    key: String,
    @Suppress("UNUSED_PARAMETER") value: Nothing?
): Unit = remove(key)