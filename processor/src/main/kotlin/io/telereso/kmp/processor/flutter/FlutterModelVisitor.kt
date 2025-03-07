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

package io.telereso.kmp.processor.flutter

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.camelToSnakeCase
import java.io.OutputStream

class FlutterModelVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() { //1

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val modelPackage = "models"
        val className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()
        val imports = mutableListOf<String>()
        classDeclaration.getAllProperties().forEach {
            if (!it.type.isFlutterType())
                imports.add(
                    "import 'package:flutter_food_client/$modelPackage/${
                        it.simpleName.asString().camelToSnakeCase()
                    }.dart';"
                )
        }
        val importsString = imports.joinToString("\n")

        val constructorMembers =
            classDeclaration.getAllProperties().map { "this.${it.simpleName.asString()}" }
                .joinToString(",")
        val members = classDeclaration.getAllProperties()
            .map {
                "  @JsonKey(name: \"${
                    it.getJsonKey()
                }\")\n  ${it.type.flutterType()}? ${it.simpleName.asString()};"
            }.joinToString("\n\n")


//        val arguments = classDeclaration.annotations.iterator().next().arguments
//        val annotatedParameter = arguments[0].value as KSType //2
//        val parcelKey = arguments[1].value as String //3

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "flutter/$modelPackage",
            fileName = className.camelToSnakeCase(),
            extensionName = "dart"
        )
        val classVariable = "\$${className}"

        outputStream.write(
            """
import 'package:json_annotation/json_annotation.dart';
$importsString

part '$snakeClassName.g.dart';

@JsonSerializable(explicitToJson: true)
class $className {
  $className($constructorMembers);

$members

  factory ${className}.fromJson(Map<String, dynamic> json) => _${classVariable}FromJson(json);

  Map<String, dynamic> toJson() => _${classVariable}ToJson(this);
}
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

