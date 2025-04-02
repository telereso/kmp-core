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

package io.telereso.kmp.processor.lists

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.annotations.SkipListWrappers
import io.telereso.kmp.processor.*
import io.telereso.kmp.processor.reactnative.getParametersAndroid
import io.telereso.kmp.processor.reactnative.getTypedParametersAndroid
import java.io.OutputStream


class ListWrapperVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() {

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val classDeclaration = function.closestClassDeclaration()!!
        val packageString = (packageName ?: classDeclaration.packageName.asString())
        val commonFlowListClass =
            REGEX_COMMON_FLOW_LIST.find(function.returnType?.resolve().toString())?.value ?: return

        val enums = hashSetOf<String>()

//        val modelsPackageString = originalPackageString.removeSuffix("client").plus("models")

        createCommonFlowFunctionsWrapper(
            packageString,
            commonFlowListClass,
            function,
            enums
        )
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        var className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        val enums = hashSetOf<String>()


        if (classDeclaration.modifiers.contains(Modifier.DATA)) {
            createModelsWrapper(packageString, className)
        } else {
            className = className.removeSuffix("Manager")
            if (className.isEmpty()) return

            classDeclaration.getAllFunctions().filter {
                it.getVisibility() == Visibility.PUBLIC
                        && !it.skipListWrapper()
                        && !skipFunctions.contains(it.simpleName.asString())
            }.forEach { function ->
                var isCommonFlow = false
                function.getCommonFlowListClass()?.let { c ->
                    isCommonFlow = true
                    createCommonFlowFunctionsWrapper(packageString, c, function, enums)
                }
                if (!isCommonFlow)
                    function.getTaskArrayClass()?.let { c ->
                        createTaskFunctionsWrapper(packageString, c, function, enums)
                    }
            }
        }
    }

    private fun createModelsWrapper(
        packageString: String,
        className: String
    ) {
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${className}ListWrappers",
            extensionName = "kt"
        )

        outputStream.write(
            """
                |package $packageString
                |
                |import io.telereso.kmp.annotations.JsOnlyExport
                |import kotlinx.serialization.Serializable
                |
                |@Serializable
                |data class ${className}List(val list: List<$className>)
                |
                |@Serializable
                |@JsOnlyExport
                |data class ${className}Array(val array: Array<${className}>) {
                |    override fun equals(other: Any?): Boolean {
                |        if (this === other) return true
                |        if (other == null || this::class != other::class) return false
                |
                |        other as ${className}Array
                |
                |        if (!array.contentEquals(other.array)) return false
                |
                |        return true
                |    }
                |
                |    override fun hashCode(): Int {
                |         return array.contentHashCode()
                |    }
                |}
            """.trimMargin().toByteArray()
        )
    }

    private fun createCommonFlowFunctionsWrapper(
        packageString: String,
        modelClass: String,
        function: KSFunctionDeclaration,
        enums: HashSet<String>
    ) {
        val modelPackageString = packageString.removeSuffix("client").plus("models")
        val originalClass = function.closestClassDeclaration()!!

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${originalClass}${
                function.simpleName.asString().snakeToUpperCamelCase()
            }Extension",
            extensionName = "kt"
        )

        val funName = function.simpleName.asString()
        val listFunName = function.getListFlowName()
        val arrayFunName = function.getArrayFlowName()

        val typedParams = function.getTypedParametersAndroid(enums, true)
        val params = function.getParametersAndroid(enums, true)

        val repositoryName = originalClass.getAllProperties().firstOrNull {
            it.type.resolve().toString() == originalClass.simpleName.asString().removeSuffix("Manager").plus("Repository")
        }?.simpleName?.asString() ?: "repository"

        val annotationsImports = function.annotations.joinToString("\n") { "import ${it.annotationType.resolve().declaration.packageName.asString()}.${it.shortName.asString()}" }
        val annotations = function.annotations.joinToString("\n") { "@${it.shortName.asString()}" }
        outputStream.write(
            """
            |package $packageString
            |
            |import io.telereso.kmp.core.CommonFlow
            |import io.telereso.kmp.core.Task
            |import io.telereso.kmp.core.Log
            |import io.telereso.kmp.core.asCommonFlow
            |import io.telereso.kmp.annotations.JsOnlyExport
            |import kotlinx.coroutines.flow.map
            |
            |import $modelPackageString.$modelClass.*
            |import $modelPackageString.${modelClass}List
            |import $modelPackageString.${modelClass}Array
            |$annotationsImports
            |
            |/**
            |* Platform : iOS, JS
            |* Origin: [$originalClass.$funName]
            |* Generated function to provide a wrapper for an array response
            |*/
            |$annotations
            |@JsOnlyExport
            |fun $originalClass.$arrayFunName($typedParams): Task<CommonFlow<${modelClass}Array>> {
            |   Log.d("$originalClass","$arrayFunName")
            |   return Task.execute {
            |       $repositoryName.$funName($params).map { ${modelClass}Array(it.toTypedArray()) }.asCommonFlow()
            |   }
            |}
            |
            |/**
            |* Platform : iOS
            |* Origin: [$originalClass.$funName]
            |* Generated function to provide a wrapper for a list response
            |*/
            |$annotations
            |fun $originalClass.$listFunName($typedParams): Task<CommonFlow<${modelClass}List>> {
            |    Log.d("$originalClass","$listFunName")
            |    return Task.execute {
            |        $repositoryName.$funName($params).map { ${modelClass}List(it) }.asCommonFlow()
            |    }
            |}
            """.trimMargin().toByteArray()
        )
    }

    private fun createTaskFunctionsWrapper(
        packageString: String,
        modelClass: String,
        function: KSFunctionDeclaration,
        enums: HashSet<String>
    ) {
        val modelPackageString = packageString.removeSuffix("client").plus("models")
        val originalClass = function.closestClassDeclaration()!!

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${originalClass}${
                function.simpleName.asString().snakeToUpperCamelCase()
            }Extension",
            extensionName = "kt"
        )

        val funName = function.simpleName.asString()
        val listWrapperFunName = function.getTaskListWrapperName()
        val arrayWrapperFunName = function.getTaskArrayWrapperName()
//        val arrayFunName = function.getTaskArrayName()

        val typedParams = function.getTypedParametersAndroid(enums, true)
        val params = function.getParametersAndroid(enums, true)

        val repositoryName = originalClass.getAllProperties().firstOrNull {
            it.type.resolve().toString() == originalClass.simpleName.asString()
                .removeSuffix("Manager").plus("Repository")
        }?.simpleName?.asString() ?: "repository"

        val annotationsImports =
            function.annotations.joinToString("\n") { "import ${it.annotationType.resolve().declaration.packageName.asString()}.${it.shortName.asString()}" }
        val annotations = function.annotations.joinToString("\n") { "@${it.shortName.asString()}" }
        outputStream.write(
            """
            |package $packageString
            |
            |import io.telereso.kmp.core.Task
            |import io.telereso.kmp.core.Log
            |
            |import $modelPackageString.*
            |import $modelPackageString.$modelClass
            |import $modelPackageString.$modelClass.*
            |import $modelPackageString.${modelClass}List
            |import $modelPackageString.${modelClass}Array
            |import io.telereso.kmp.annotations.JsOnlyExport
            |$annotationsImports
            |
            |/**
            |* Platform : iOS, JS
            |* Origin: [$originalClass.$funName]
            |* Generated function to provide a wrapper for an array response
            |*/
            |$annotations
            |@JsOnlyExport
            |fun $originalClass.$arrayWrapperFunName($typedParams): Task<${modelClass}Array> {
            |   Log.d("$originalClass","$funName")
            |   return Task.execute {
            |       ${modelClass}Array($repositoryName.$funName($params).toTypedArray())
            |   }
            |}
            |
            |/**
            |* Platform : iOS
            |* Origin: [$originalClass.$funName]
            |* Generated function to provide a wrapper for a list response
            |*/
            |$annotations
            |fun $originalClass.$listWrapperFunName($typedParams): Task<${modelClass}List> {
            |    Log.d("$originalClass","$funName")
            |    return Task.execute {
            |        ${modelClass}List($repositoryName.$funName($params))
            |    }
            |}
            """.trimMargin().toByteArray()
        )
    }

    private fun KSFunctionDeclaration.skipListWrapper(): Boolean {
        return annotations.any { it.shortName.asString() == SkipListWrappers::class.simpleName }
    }

}