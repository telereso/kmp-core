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

package io.telereso.kmp.processor.model

import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.camelToSnakeCase
import java.io.OutputStream

class ModelVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() {
    companion object {
        var shouldGenerateUtilFile = true
        var convertersClassName = ""
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        if(shouldGenerateUtilFile){
            convertersClassName = classDeclaration.simpleName.asString()
        }

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${className}Converters",
            extensionName = "kt"
        )
        val filePackageString = packageString.let {
            if (it.isBlank()) "" else "package $it"
        }

        val importClassString = filePackageString.let {
            if (it.isBlank()) "" else "import $packageString.$className"
        }

        val internalString = if(classDeclaration.isInternal()) "internal " else ""

        val jsExportAnnotation = classDeclaration.annotations.firstOrNull {
            it.shortName.asString() == "JsExport"
        }
        val jsOnlyExportAnnotation = classDeclaration.annotations.firstOrNull {
            it.shortName.asString() == "JsOnlyExport"
        }

        val commentJsExport = if (jsExportAnnotation != null) "" else "// "
        val commentJsOnlyExport = if (jsOnlyExportAnnotation != null) "" else "// "
        val commentJsName = if (jsExportAnnotation != null || jsOnlyExportAnnotation != null) "" else "// "


        outputStream.write(
            """
            |$filePackageString
            |
            |import kotlinx.serialization.builtins.ListSerializer
            |import kotlinx.serialization.decodeFromString
            |import kotlinx.serialization.json.Json
            |${commentJsOnlyExport}import io.telereso.kmp.annotations.JsOnlyExport
            |${commentJsExport}import kotlin.js.JsExport
            |${commentJsName}import kotlin.js.JsName
            |
            |${commentJsExport}@JsExport()
            |${commentJsOnlyExport}@JsOnlyExport()
            |${commentJsName}@JsName("${className}ToJson")
            |${internalString}fun $className.toJson(): String {
            |   return ${convertersClassName}JsonSerializer.encodeToString($className.serializer(), this)
            |}
            |
            |${commentJsExport}@JsExport()
            |${commentJsOnlyExport}@JsOnlyExport()
            |${commentJsName}@JsName("${className}ToJsonPretty")
            |${internalString}fun $className.toJsonPretty(): String {
            |   return ${convertersClassName}JsonPrettySerializer.encodeToString($className.serializer(), this)
            |}
            |
            |${commentJsExport}@JsExport
            |${commentJsOnlyExport}@JsOnlyExport
            |${commentJsName}@JsName("${className}FromJson")
            |${internalString}fun $className.Companion.fromJson(json:String?): $className{
            |   return ${convertersClassName}JsonSerializer.decodeFromString(json ?: "{}")
            |}
            |
            |${commentJsExport}@JsExport
            |${commentJsOnlyExport}@JsOnlyExport
            |${commentJsName}@JsName("${className}ToJsonArray")
            |${internalString}fun $className.Companion.toJson(array: Array<$className>): String {
            |   return ${convertersClassName}JsonSerializer.encodeToString(ListSerializer($className.serializer()), array.toList())
            |}
            |
            |${commentJsExport}@JsExport
            |${commentJsOnlyExport}@JsOnlyExport
            |${commentJsName}@JsName("${className}FromJsonArray")
            |${internalString}fun $className.Companion.fromJsonArray(json:String?): Array<$className> {
            |   return ${convertersClassName}JsonSerializer.decodeFromString(ListSerializer($className.serializer()), json ?: "[]").toTypedArray()
            |}
            |
            |
            """.trimMargin().toByteArray()
        )


        if (shouldGenerateUtilFile) {
            generateUtilFile(codeGenerator, packageString, filePackageString)
            shouldGenerateUtilFile = false
        }
    }

    private fun generateUtilFile(
        codeGenerator: CodeGenerator,
        packageString: String,
        filePackageString: String
    ) {
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "Utils",
            extensionName = "kt"
        )


        outputStream.write(
            """
            |$filePackageString
            |
            |import kotlinx.serialization.json.Json
            |
            |val ${convertersClassName}JsonSerializer = Json {
            |        prettyPrint = false
            |        isLenient = true
            |        ignoreUnknownKeys = true
            |}
            |
            |val ${convertersClassName}JsonPrettySerializer = Json {
            |        prettyPrint = true
            |        isLenient = true
            |        ignoreUnknownKeys = true
            |}
            |
            """.trimMargin().toByteArray()
        )
    }
}



private fun KSPropertyDeclaration.getJsonKey(): String {
    return annotations.find { it.toString() == "@SerialName" }
        ?.arguments?.find { it.name?.asString() == "value" }
        ?.value?.toString()
        ?: simpleName.asString()
}

private fun KSTypeReference.isFlutterType(): Boolean {
    this.resolve()
    return when (this.flutterType()) {
        "Object", "double", "String", "bool", "int" -> true
        else -> false
    }
}

private fun KSTypeReference.flutterType(): String {
    this.resolve()
    return when ("$this") {
        "Int" -> "int"
        "Boolean" -> "bool"
        else -> this.toString()
    }
}

