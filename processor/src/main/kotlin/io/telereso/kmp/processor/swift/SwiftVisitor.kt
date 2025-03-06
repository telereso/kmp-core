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

package io.telereso.kmp.processor.swift

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.getImportPackages
import io.telereso.kmp.processor.snakeToUpperCamelCase
import java.io.OutputStream

class SwiftVisitor(
    environment: SymbolProcessorEnvironment,
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    private val packageName: String? = null
) : KSVisitorVoid() {

    private val createObjectFunctionName =
        environment.options["createObjectFunctionName"] ?: "`object`"

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        classDeclaration.primaryConstructor?.let {
            visitFunctionDeclaration(it, data)
        }
    }


    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {

        val classDeclaration = function.closestClassDeclaration() ?: return

        val packageString = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.getShortName()

        createExtensionsFunctions(
            !function.isConstructor(),
            packageString,
            className,
            if (function.isConstructor()) className else function.simpleName.asString(),
            if (function.isConstructor())
                "Constructor"
            else
                function.simpleName.asString().snakeToUpperCamelCase(),
            if (function.isConstructor())
                "Companion.${createObjectFunctionName}"
            else
                function.simpleName.asString(),
            function.parameters,
            function.annotations
        )
    }

    private fun createExtensionsFunctions(
        isFunction: Boolean,
        packageString: String,
        className: String,
        invokeClassName: String,
        fileNameMiddle: String,
        functionName: String,
        parameters: List<KSValueParameter>,
        annotations: Sequence<KSAnnotation>,
    ) {

        val filePackageString = packageString.let {
            if (it.isBlank()) "" else "package $it"
        }

        val importClassString = filePackageString.let {
            if (it.isBlank()) "" else "import $packageString.$className.*"
        }

        val importPackageString = filePackageString.let {
            if (it.isBlank()) "" else "import $packageString.*"
        }

        val imports = hashSetOf<String>()

        val optionalParams = mutableListOf<KSValueParameter>()
        val requiredParams = mutableListOf<KSValueParameter>()
        val functions = mutableListOf<String>()

        parameters.forEach {
            it.name ?: return@forEach
            val type = it.type.resolve()
            val importPackage = type.declaration.packageName.asString()

            if (importPackage != "kotlin" && type.declaration.closestClassDeclaration()?.classKind != ClassKind.ENUM_CLASS)
                imports.add("$importPackage.${type.toString().removeSuffix("?").split("<")[0]}")

            imports.addAll(type.getImportPackages())

            if (it.hasDefault)
                optionalParams.add(it)
            else
                requiredParams.add(it)
        }

        val jsExportAnnotation = annotations.firstOrNull {
            it.shortName.asString() == "JsExport"
        }

        if (optionalParams.isNotEmpty()) {

            val outputStream: OutputStream = codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageString,
                fileName = "${className}${fileNameMiddle}Overloads",
                extensionName = "kt"
            )

            fun getFunction(accumulatedOptionalParams:List<KSValueParameter>): String {
                val typedParams =
                    requiredParams.plus(accumulatedOptionalParams).joinToString(", ") {
                        "${it.name?.asString()}: ${it.type.resolve()}"
                    }

                val mappedParams =
                    requiredParams.plus(accumulatedOptionalParams).joinToString(", ") {
                        "${it.name?.asString()} = ${it.name?.asString()}"
                    }

                return "fun $className.$functionName($typedParams) = $invokeClassName($mappedParams)"
            }

            val accumulatedOptionalParams = mutableListOf<KSValueParameter>()

            functions.add(getFunction(emptyList()))

            optionalParams.forEach { param ->
                accumulatedOptionalParams.add(param)

                if ((!isFunction || (requiredParams.size + accumulatedOptionalParams.size != parameters.size)))
                    functions.add(getFunction(accumulatedOptionalParams))
            }

            outputStream.write(
                """
            |$filePackageString
            |
            |$importPackageString
            |$importClassString
            |
            |${functions.joinToString("\n\n")}
            |
            """.trimMargin().toByteArray()
            )
        }
    }
}
