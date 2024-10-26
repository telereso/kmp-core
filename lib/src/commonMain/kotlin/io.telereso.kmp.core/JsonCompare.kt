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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive



fun JsonElement.compare(
    jsonElement: JsonElement,
    tolerate: TolerancePolicy = TolerancePolicy()
): Boolean {
    return compareJson(this, jsonElement, tolerate = tolerate).final().match
}

fun JsonElement.compareCheck(
    jsonElement: JsonElement,
    tolerate: TolerancePolicy = TolerancePolicy(),
    printObjects: Boolean = false
) {
    compareJson(this, jsonElement, tolerate = tolerate).finalCheck(printObjects)
}

data class TolerancePolicy(
    val missingAsNull: Boolean = false,
    val decimalAsDouble: Boolean = false,
)

data class CompareResult(
    val match: Boolean, val differences: MutableList<Difference> = mutableListOf()
) {
    fun final(): CompareResult {
        return CompareResult(this.differences.isEmpty(), differences)
    }

    fun finalCheck(printObjects: Boolean = false) {
        final().apply {
            if (differences.isNotEmpty()) {
                throw Throwable("JsonElements don't match, differences:\n" + differences.joinToString(
                    "\n"
                ) { it.message.plus(if (printObjects) "\n - ${it.obj1}\n - ${it.obj2}" else "") })
            }
        }
    }
}

data class Difference(val obj1: String, val obj2: String, val path: String, val message: String)

fun compareJson(
    json1: JsonElement,
    json2: JsonElement,
    path: String = "",
    compareResult: CompareResult = CompareResult(false),
    tolerate: TolerancePolicy = TolerancePolicy()
): CompareResult {
    if (json1::class != json2::class) {
        compareResult.differences.add(
            Difference(
                json1.toString(),
                json2.toString(),
                path,
                "Type mismatch at $path: ${json1::class} vs ${json2::class}",
            )
        )
        return compareResult.copy(match = false)
    }

    return when (json1) {
        is JsonObject -> compareJsonObjects(
            compareResult, tolerate, json1, json2 as JsonObject, path
        )

        is JsonArray -> compareJsonArrays(compareResult, tolerate, json1, json2 as JsonArray, path)
        is JsonPrimitive -> compareJsonPrimitives(
            compareResult,
            tolerate,
            json1,
            json2 as JsonPrimitive,
            path
        )

        is JsonNull -> compareResult.copy(match = true)
        else -> compareResult.copy(match = false)
    }
}

fun compareJsonObjects(
    compareResult: CompareResult,
    tolerate: TolerancePolicy,
    obj1: JsonObject,
    obj2: JsonObject,
    path: String
): CompareResult {
    var equal = true
    val keys = obj1.keys.union(obj2.keys)
    for (key in keys) {
        val newPath = if (path.isEmpty()) key else "$path.$key"
        var value1 = obj1[key]
        var value2 = obj2[key]

        if (tolerate.missingAsNull) {
            if (value1 == null && value2 == JsonNull)
                value1 = JsonNull

            if (value2 == null && value1 == JsonNull)
                value2 = JsonNull
        }

        if (value1 == null) {
            compareResult.differences.add(
                Difference(
                    obj1.toString(),
                    obj2.toString(),
                    newPath,
                    "Missing key in first JSON at $newPath",
                )
            )
            equal = false
        } else if (value2 == null) {
            compareResult.differences.add(
                Difference(
                    obj1.toString(),
                    obj2.toString(),
                    newPath,
                    "Missing key in second JSON at $newPath",
                )
            )
            equal = false
        } else {
            equal = compareJson(value1, value2, newPath, compareResult, tolerate).match && equal
        }
    }
    return compareResult.copy(match = equal)
}

fun compareJsonArrays(
    compareResult: CompareResult,
    tolerate: TolerancePolicy,
    array1: JsonArray,
    array2: JsonArray,
    path: String
): CompareResult {
    if (array1.size != array2.size) {
        compareResult.differences.add(
            Difference(
                array1.toString(),
                array2.toString(),
                path,
                "Array size mismatch at $path: ${array1.size} vs ${array2.size}",
            )
        )
        return compareResult.copy(match = false)
    }
    var equal = true
    for (i in array1.indices) {
        val newPath = "$path[$i]"
        equal = compareJson(array1[i], array2[i], newPath, compareResult, tolerate).match && equal
    }
    return compareResult.copy(match = equal)
}

fun compareJsonPrimitives(
    compareResult: CompareResult,
    tolerate: TolerancePolicy,
    primitive1: JsonPrimitive,
    primitive2: JsonPrimitive,
    path: String
): CompareResult {
    var content1 = primitive1.content
    var content2 = primitive2.content
    if (tolerate.decimalAsDouble){
        if (content1.toIntOrNull() != null && content2.toIntOrNull() == null)
            content1 = content1.toDoubleOrNull()?.toString() ?: content1
        else if (content2.toIntOrNull() != null && content1.toIntOrNull() == null)
            content2 = content2.toDoubleOrNull()?.toString() ?: content2

//        // workaround for js
        if (content1 != content2) {
            if (content1.endsWith(".0") && content1.startsWith(content2))
                content2 = "$content2.0"
            else if (content2.endsWith(".0") && content2.startsWith(content1))
                content1 = "$content1.0"
        }

    }
    if (content1 != content2) {
        compareResult.differences.add(
            Difference(
                primitive1.toString(),
                primitive2.toString(),
                path,
                "Value mismatch at $path: ${primitive1.content} vs ${primitive2.content}",
            )
        )
        return compareResult.copy(match = false)
    }
    return compareResult.copy(match = true)
}