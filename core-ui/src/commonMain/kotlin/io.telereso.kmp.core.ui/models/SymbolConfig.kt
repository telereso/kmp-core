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

package io.telereso.kmp.core.ui.models

import io.telereso.kmp.core.ui.models.SymbolConfig.Companion.host
import io.telereso.kmp.core.ui.models.SymbolConfig.Companion.protocol
import io.telereso.kmp.core.ui.models.SymbolConfig.Type

class SymbolConfig(
    val name: String,
    private var type: Type = Type.OUTLINED,
    private var isFilled: Boolean = false,
    internal var size: Size = Size._24,
    private var grade: Grade? = null,
    private var weight: Weight? = null
) {

    companion object {
        internal var protocol = "https"
        internal var host =
            "raw.githubusercontent.com/google/material-design-icons/refs/heads/master"



        fun setHost(host: String) {
            this.host = host
        }

        fun setProtocol(protocol: String) {
            this.protocol = protocol
        }
    }

    enum class Type {
        OUTLINED, SHARP, ROUNDED
    }

    enum class Size {
        _20, _24, _40, _48
    }

    enum class Grade {
        N25, _200
    }

    enum class Weight {
        _100, _200, _300, _500, _600, _700
    }

    fun outlined(): SymbolConfig {
        type = Type.OUTLINED
        return this
    }

    fun sharp(): SymbolConfig {
        type = Type.SHARP
        return this
    }

    fun rounded(): SymbolConfig {
        type = Type.ROUNDED
        return this
    }

    fun filled(): SymbolConfig {
        isFilled = true
        return this
    }

    fun grade(value: Grade): SymbolConfig {
        grade = value
        return this
    }

    fun size(value: Size): SymbolConfig {
        size = value
        return this
    }

    fun weight(value: Weight): SymbolConfig {
        weight = value
        return this
    }

    internal fun url(): String {
        return getUrlFromSymbolConfig(
            name = name,
            type = type,
            size = size,
            isFilled = isFilled,
            weight = weight,
            grade = grade
        )
    }
}

fun getUrlFromSymbolConfig(
    name: String,
    type: Type,
    isFilled: Boolean,
    size: SymbolConfig.Size,
    weight: SymbolConfig.Weight?,
    grade: SymbolConfig.Grade?
): String {
    val typeString = when (type) {
        Type.OUTLINED -> "outlined"
        Type.SHARP -> "sharp"
        Type.ROUNDED -> "rounded"
    }
    val weightString =
        if (weight == null) "" else "wght${weight.name.removePrefix("_")}"
    val gradeString =
        if (grade == null) "" else "grad${grade.name.removePrefix("_")}"
    val fillString = if (!isFilled) "" else "fill1"
    var variant = "${weightString}${gradeString}${fillString}"
    if (variant.isNotBlank())
        variant += "_"

    val finalName =
        "${name}_${variant}${size.name.removePrefix("_")}px.svg"

    return "$protocol://$host/symbols/web/${name.removePrefix("_")}/materialsymbols${typeString}/$finalName"
}

object Symbols

