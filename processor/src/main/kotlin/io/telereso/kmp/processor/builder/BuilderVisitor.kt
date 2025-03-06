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

package io.telereso.kmp.processor.builder

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.camelToSnakeCase
import io.telereso.kmp.processor.getImportPackages
import java.io.OutputStream


class BuilderVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() {

    var shouldGenerateUtilFile = true

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()


        runCatching {
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${className}Builder",
            extensionName = "kt"
        )
        val filePackageString = packageString.let {
            if (it.isBlank()) "" else "package $it"
        }

        val importClassString = filePackageString.let {
            if (it.isBlank()) "" else "import $packageString.$className.*"
        }

        val builderClassName = "${className}Builder"

        val internalString = if (classDeclaration.isInternal()) "internal " else ""

        val imports = hashSetOf<String>()
        val properties = mutableListOf<String>()
        val setters = mutableListOf<String>()
        val buildNoneNullableParams = mutableListOf<KSValueParameter>()
        val buildNullableParams = mutableListOf<String>()


        val parameters = classDeclaration.getConstructors().first().parameters
        parameters.forEach {
            it.name ?: return@forEach
            val type = it.type.resolve()
            val importPackage = type.declaration.packageName.asString()

            if (importPackage != "kotlin" && type.declaration.closestClassDeclaration()?.classKind != ClassKind.ENUM_CLASS)
                imports.add("$importPackage.${type.toString().removeSuffix("?").split("<")[0]}")

            imports.addAll(type.getImportPackages())


            if (type.isMarkedNullable) {
                properties.add(it.getProperty(type))
                setters.add(it.getSetterFunction(builderClassName))
                buildNullableParams.add(it.name?.asString() ?: "")
            } else {
                buildNoneNullableParams.add(it)
            }
        }

        val jsExportAnnotation = classDeclaration.annotations.firstOrNull {
            it.shortName.asString() == "JsExport"
        }

        val importJsExport = if (jsExportAnnotation != null) "import kotlin.js.JsExport" else ""
        val jsExportString = if (jsExportAnnotation != null) "@JsExport" else ""

            outputStream.write(
                """
            |$filePackageString
            |
            |$importClassString
            |$importJsExport
            |${imports.joinToString("\n") { "import $it" }}
            |import kotlin.jvm.JvmStatic
            |
            |$jsExportString
            |${internalString}class ${builderClassName}(${buildNoneNullableParams.joinToString(", ") { "private val _${it.name?.asString()}: ${it.type.resolve()}" }}) {
            |${properties.joinToString("\n")}
            |
            |${setters.joinToString("")}
            |    fun build(): $className {
            |        return $className(${
                    parameters.joinToString(",") { "_${it.name?.asString()}" }
                })
            |    } 
            |    
            |    companion object {
            |      @JvmStatic
            |      fun builder(${buildNoneNullableParams.joinToString(", ") { "${it.name?.asString()}: ${it.type.resolve()}" }}): $builderClassName {
            |          return $builderClassName(${buildNoneNullableParams.joinToString(",")})
            |      }
            |    }
            |    
            |}
            """.trimMargin().toByteArray()
            )
        }

    }

}


private fun KSValueParameter.getProperty(type: KSType): String {
    val name = name?.asString() ?: ""
    val nullable = if (type.isMarkedNullable) "? = null" else ""
    return "    private var _${name}: ${type.toString().removeSuffix("?")}$nullable"
}

private fun KSValueParameter.getSetterFunction(builderClassName: String): String {
    val type = type.resolve()
    val nullable = if (type.isMarkedNullable) "?" else ""
    val name = name?.asString() ?: ""
    var res = """
            |    fun ${name}(${name}: ${type}): $builderClassName {
            |        _$name = $name
            |        return this
            |    }
            |        
            |""".trimMargin()

    if (type.toString().startsWith("Array"))
        res += """
            |    fun ${name}List(${name}: ${
            type.toString().replaceFirst("Array", "List")
        }): $builderClassName {
            |        _$name = ${name}${nullable}.toTypedArray()
            |        return this
            |    }
            |        
            |""".trimMargin()

    return res
}
