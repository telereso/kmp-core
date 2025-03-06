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

package io.telereso.kmp.processor.reactnative

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.annotations.SkipReactNativeExport
import io.telereso.kmp.processor.*
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


const val CLASS_EMITTER_SUBSCRIPTION = "EmitterSubscription"

class ReactNativeMangerVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    private val scope: String? = null,
    private val packageName: String? = null
) : KSVisitorVoid() {

    private val skipClasses =
        listOf(CLASS_EMITTER_SUBSCRIPTION, "void", "CommonFlow", "String", "Boolean", "Int", "Unit")

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {

        createAndroidManager(classDeclaration)
        createIosManager(classDeclaration)
        createReactNativeIndex(classDeclaration)
    }

    private fun createAndroidManager(classDeclaration: KSClassDeclaration) {

        val packageString = packageName ?: classDeclaration.packageName.asString()
        val modelsPackageString = packageString.removeSuffix(".client").plus(".models")
        val originalClassName = classDeclaration.simpleName.getShortName()

        logger.logging("Create Android Manager for $originalClassName")


        val className = originalClassName.removeSuffix("Manager")
        if (className.isEmpty()) return

        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "rn-kotlin.${packageString}.rn",
            fileName = "${className}Module",
            extensionName = "g.kt"
        )

        val enums = hashSetOf<String>()
        val functionsImports = mutableListOf<String>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC &&
                !it.skipReactNative() &&
                !skipFunctions.contains(it.simpleName.asString())
            ) {
                it.getCommonFlowListClass()?.let { _ ->
                    functionsImports.add(it.getListFlowName())
                    functionsImports.add(it.getArrayFlowName())
                }

                it.getMethodBodyAndroid(enums).second
            } else null
        }


        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())
            ) {
                """  
                |  @ReactMethod
                |  fun ${it.simpleName.asString()}(${
                    it.getTypedParametersAndroid(enums)
                        .let { params -> if (params.isNotBlank()) "$params, " else params }
                }promise: Promise) {
                |${it.getMethodBodyAndroid(enums).first}
                |  }
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${modelsPackageString}.models.$it" }

        val importExtensionFunctions = functionsImports.joinToString("\n") {
            "import $packageString.$it"
        }

        outputStream.write(
            """
            |package $packageString.rn
            |
            |import $packageString.$originalClassName
            |import $modelsPackageString.*
            |$importExtensionFunctions
            |import com.facebook.react.bridge.*
            |import com.facebook.react.modules.core.DeviceEventManagerModule
            |import kotlin.js.ExperimentalJsExport
            |import io.telereso.kmp.core.Task
            |
            |
            |@OptIn(ExperimentalJsExport::class)
            |class ${className}Module(reactContext: ReactApplicationContext) :
            |  ReactContextBaseJavaModule(reactContext) {
            |
            |  private val manager = $originalClassName.getInstance()
            |
            |  override fun getName(): String {
            |    return NAME
            |  }
            |
            |${methods.joinToString("\n\n")}
            |
            |  companion object {
            |    const val NAME = "$className"
            |  }
            |}
            """.trimMargin().toByteArray()
        )
    }

    private fun createIosManager(classDeclaration: KSClassDeclaration) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.getShortName()
        val className = originalClassName.removeSuffix("Manager")
        if (className.isEmpty()) return

        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        logger.logging("Create iOS Manager for $originalClassName")

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "ios",
            fileName = className,
            extensionName = "swift"
        )

        val enums = HashMap<String, List<String>>()

        val flowEvents = mutableListOf<String>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())) {
                val res = it.getMethodBodyIos(className, enums)
                flowEvents.addAll(res.third)
                res.second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())) {
                """
                |    @objc(${
                    it.getTypedHeaderParametersIos()
                })
                |    func ${it.simpleName.asString()}(${
                    it.getTypedParametersIos(enums)
                        .let { params -> if (params.isNotBlank()) "$params, " else "_ " }
                }resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
                |        ${it.getMethodBodyIos(className,enums).first}
                |    }
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }

        outputStream.write(
            """
            |import $className
            |
            |extension String: Error {
            |}
            |
            |
            |@objc($className)
            |class $className: RCTEventEmitter {
            |
            |    private var hasListeners = false;
            |
            |    override func supportedEvents() -> [String]! {
            |        return [${flowEvents.joinToString(",") { "\"$it\"" }}]
            |    }
            |
            |    override func startObserving() {
            |        hasListeners = true
            |    }
            |
            |    override func stopObserving() {
            |        hasListeners = false
            |    }
            |
            |    override func sendEvent(withName name: String!, body: Any!) {
            |        if (hasListeners) {
            |            super.sendEvent(withName: name, body: body)
            |        }
            |    }
            |
            |${methods.joinToString("\n\n")}
            |
            |}
            """.trimMargin().toByteArray()
        )

        createIosManagerHeader(classDeclaration)
    }

    private fun createIosManagerHeader(classDeclaration: KSClassDeclaration) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.getShortName()
        val className = originalClassName.removeSuffix("Manager")
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        logger.logging("Create iOS Manager Header for $originalClassName")

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "ios",
            fileName = className,
            extensionName = "m"
        )

        val enums = hashMapOf<String, List<String>>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyIos(className, enums).second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())) {
                """
                |RCT_EXTERN_METHOD(${it.getTypedHeaderParametersIosOC()})
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }

        outputStream.write(
            """
            |#import <React/RCTBridgeModule.h>
            |#import <React/RCTEventEmitter.h>
            |
            |@interface RCT_EXTERN_MODULE($className, RCTEventEmitter)
            |
            |RCT_EXTERN_METHOD(supportedEvents)
            |
            |${methods.joinToString("\n\n")}
            |
            |+ (BOOL)requiresMainQueueSetup
            |{
            |  return NO;
            |}
            |
            |@end
            |
            """.trimMargin().toByteArray()
        )
    }

    private fun createReactNativeIndex(classDeclaration: KSClassDeclaration) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.getShortName()
        val className = originalClassName.removeSuffix("Manager")
        if (className.isEmpty()) return

        val projectName = className.removeSuffix("Client").lowercase()
        val modelPackage = packageString.removeSuffix(".client").plus(".models")
        val modelClassName = projectName.snakeToUpperCamelCase().plus("Models")
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        logger.logging("Create JS Manager for $originalClassName")

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "js",
            fileName = "index",
            extensionName = "tsx"
        )

        val modelImports = hashSetOf<String>()
        val modelFromJsonImports = hashSetOf<String>()
        val modelToJsonImports = hashSetOf<String>()

        val enums = hashMapOf<String, List<String>>()

        classDeclaration.getAllFunctions().forEach {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())) {
                val typedParams = it.getTypedParametersJs(enums).second
                it.getMethodBodyJs(className, enums).let { trip ->
                    trip.second.forEach { promisedKlass ->
                        val klass = promisedKlass
                            .removePrefix("Promise<")
                            .removePrefix("typeof ")
                            .removeSuffix(">")
                        typedParams.add(klass)
                        modelFromJsonImports.add(klass)
                    }
                    modelToJsonImports.addAll(trip.third)
                }
                modelImports.addAll(typedParams)

                it.getCommonFlowListClass()?.let { c->
                    val arrayClassName = "${c}Array"
                    modelImports.add(arrayClassName)
                    modelFromJsonImports.add(arrayClassName)
                }

            }
        }

        val modelImportString =
            modelImports
                .filter { m -> !skipClasses.any { s -> m.startsWith(s, true) } && !m.contains(".") }
                .map { it.removeSuffix("?") }
                .distinct()
                .joinToString("\n") {
                """
                const $it = ${modelClassName}.${it};
                """.trimIndent()
            }

        val modelJsonImportString =
            modelFromJsonImports
                .filter { m -> !skipClasses.any { s -> m.startsWith(s, true) } && !m.contains(".") }
                .map { it.removeSuffix("?") }
                .distinct()
                .joinToString("\n") {
                """
                // @ts-ignore
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                const ${it}FromJson = ${modelClassName}.${it}FromJson;

                // @ts-ignore
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                const ${it}FromJsonArray = ${modelClassName}.${it}FromJsonArray;
                """.trimIndent()
            }

        val modelToJsonImportsString =
            modelToJsonImports.joinToString("\n") {
                """
                const ${it}ToJson = ${modelClassName}.${it}ToJson;
                """.trimIndent()
            }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC
                && !it.skipReactNative()
                && !skipFunctions.contains(it.simpleName.asString())) {
                val res = it.getMethodBodyJs(className, enums)
                val typedParams = it.getTypedParametersJs(enums)
                res.second.forEach { klass: String ->
                    modelImports.add(klass)
                }
                modelImports.addAll(typedParams.second)


                """
                |export function ${it.simpleName.asString()}(${typedParams.first}): ${res.second.first()} {
                |  ${res.first}
                |}
                |
                """.trimMargin()
            } else
                null
        }

        outputStream.write(
            """
            |import { NativeModules, Platform, NativeEventEmitter, $CLASS_EMITTER_SUBSCRIPTION } from 'react-native';
            |
            |const LINKING_ERROR =
            |  `The package 'react-native-${projectName}-client' doesn't seem to be linked. Make sure: \n\n` +
            |  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
            |  '- You rebuilt the app after installing the package\n' +
            |  '- You are not using Expo Go\n';
            |
            |const ${projectName.snakeToUpperCamelCase()}Models = require('${scope?.let { "@$it/" } ?: ""}$projectName-models').${modelPackage};
            |
            |
            |const $className = NativeModules.${className}
            |  ? NativeModules.${className}
            |  : new Proxy(
            |      {},
            |      {
            |        get() {
            |          throw new Error(LINKING_ERROR);
            |        },
            |      }
            |    );
            |    
            |$modelImportString
            |$modelJsonImportString
            |$modelToJsonImportsString
            |
            |${methods.joinToString("\n\n")}
            |
            """.trimMargin().toByteArray()
        )
    }

    private fun KSFunctionDeclaration.skipReactNative(): Boolean {
        return annotations.any { it.shortName.asString() == SkipReactNativeExport::class.simpleName }
    }

}

private fun KSFunctionDeclaration.getParametersSignature(): String {
    return "${parameters.size}${
        parameters.joinToString("") { "${(it.name?.asString() ?: "n")[0]}${it.type.toString()[0]}".lowercase() }
    }"
}

private fun KSFunctionDeclaration.getResultAndroid(enums: HashSet<String>): Pair<String, String?> {


    val type = returnType?.resolve()
    val typeString = type?.toString()
    return when {
        typeString == null -> "it" to null
        typeString.startsWith(CLASS_COMMON_FLOW) -> {
            val commonFlowClass = REGEX_COMMON_FLOW.find(typeString)?.value
            if (commonFlowClass?.isPrimitiveKotlin() == true) {
                "it" to null
            } else {
                val nullabilityString = if (commonFlowClass?.endsWith("?") == true) "?" else ""
                "it${nullabilityString}.toJson()" to commonFlowClass
            }
        }
        typeString.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(typeString)?.value

            "${klass}.toJson(it)" to klass
        }
        typeString.startsWith(PREFIX_TASK) -> {
            var klass = REGEX_TASK.find(typeString)?.value
            val commonFlowClass = REGEX_COMMON_FLOW.find(typeString)?.value
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(typeString)?.value
            val commonFlowArrayClass = REGEX_COMMON_FLOW_ARRAY.find(typeString)?.value

            val nullabilityString = if (klass?.endsWith("?") == true) "?" else ""
//            val nullabilityString = when (type.nullability) {
//                Nullability.NULLABLE -> "?"
//                else -> ""
//            }
            var res = "it"

            when {
                klass == null -> {}
                klass == "Unit" -> {
                    res = "\"\""
                }
                !commonFlowArrayClass.isNullOrEmpty() -> {
                    klass = commonFlowArrayClass
                    res = "${klass}.toJson(it)"
                }
                !commonFlowListClass.isNullOrEmpty() -> {
                    klass = commonFlowListClass
                    res = "${res}${nullabilityString}.toJson()"
                }
                !commonFlowClass.isNullOrEmpty() -> {
                    klass = commonFlowClass
                    res = "${res}${nullabilityString}.toJson()"
                }
                enums.contains(klass) -> {}
                !klass.isPrimitiveKotlin() -> {
                    res = "${res}${nullabilityString}.toJson()"
                }
            }

            if (klass?.isPrimitiveKotlin() == true)
                klass = null

            res to klass
        }
        else -> {
            "it" to null
        }
    }
}

private fun KSFunctionDeclaration.getResultIos(): Pair<String, String?> {

    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "res" to null
        type == "Unit" -> "\"\"" to null
        type.startsWith(CLASS_COMMON_FLOW) -> {
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value
            if (commonFlowClass?.isPrimitiveKotlin() == true) {
                "res" to returnType?.objectiveCType()
            } else {
                "res.toJson()" to commonFlowClass
            }
        }
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value
            "${klass}.Companion().toJson(array: res)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            var klass = REGEX_TASK.find(type)?.value
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(type)?.value
            val commonFlowArrayClass = REGEX_COMMON_FLOW_ARRAY.find(type)?.value


            klass = (commonFlowListClass ?: commonFlowArrayClass ?: commonFlowClass
            ?: klass)?.removeSuffix("?")

            when {
                !commonFlowListClass.isNullOrEmpty() -> {
                    if (klass?.isPrimitiveKotlin() == true)
                        "res" to null
                    else
                        "res.toJson()" to "${klass}Array"
                }
                !commonFlowArrayClass.isNullOrEmpty() -> {
                    if (klass?.isPrimitiveKotlin() == true)
                        "res" to null
                    else
                        "${klass}.Companion().toJson(array: res)" to "KotlinArray<$klass>"
                }
                !commonFlowClass.isNullOrEmpty() -> {
                    if (klass?.isPrimitiveKotlin() == true)
                        "res" to null
                    else
                        "res.toJson()" to klass
                }
                klass == null || klass == "Unit" -> "\"\"" to null
                klass in listOf("String", "Long", "Int", "Boolean") -> "res" to null
                else -> {
                    "res.toJson()" to null
                }
            }
        }
        else -> {
            "res" to null
        }
    }
}

private fun KSFunctionDeclaration.getResultJs(): Pair<String, String?> {

    val type = returnType?.resolve()?.toString()
    return when {
        type == null || type == "Unit" -> "" to null
        type.startsWith("String") -> "data" to "String"
        type.startsWith("Boolean") -> "data" to "boolean"
        type.startsWith("Int") -> "data" to "number"
        type.startsWith(CLASS_COMMON_FLOW) -> {
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value?.removeSuffix("?")
            if (commonFlowClass?.isPrimitiveKotlin() == true)
                "data" to null
            else
                "${commonFlowClass}FromJson(${commonFlowClass}.Companion, data)" to commonFlowClass
        }
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value?.jsType()?.removePrefix("typeof ")
            "${klass}FromJsonArray(${klass}.Companion, data)" to klass
        }
        type.startsWith(PREFIX_TASK_LIST) -> {
            var klass = REGEX_TASK_LIST.find(type)?.value?.jsType()?.removePrefix("typeof ")
            klass = "${klass}Array"
            "${klass}FromJson(${klass}.Companion, data)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            var klass = REGEX_TASK.find(type)?.value?.jsTypeClass()
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(type)?.value
            val commonFlowArrayClass = REGEX_COMMON_FLOW_ARRAY.find(type)?.value


            if (commonFlowClass != null)
                klass = commonFlowClass.removeSuffix("?")

            if (commonFlowListClass != null)
                klass = "${commonFlowListClass.removeSuffix("?")}Array"

            when {
                !commonFlowArrayClass.isNullOrEmpty() -> {
                    klass = commonFlowArrayClass.removeSuffix("?")
                    "${klass}FromJsonArray(${klass}.Companion, data)" to klass
                }
                else -> {
                    when (klass) {
                        "Unit", "unit" -> "" to null
                        "string", "boolean", "number" -> "data" to klass
                        else -> {
                            "${klass}FromJson(${klass}.Companion, data)" to klass
                        }
                    }
                }
            }
        }
        else -> {
            "data" to null
        }
    }
}

private fun KSFunctionDeclaration.getMethodBodyAndroid(enums: HashSet<String>): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    val funName = simpleName.asString()
    var funNameList: String? = null
    val escapedName = "\${NAME}"

    return when {
        type == null -> "" to null
        type.startsWith(CLASS_COMMON_FLOW) -> {
            val res = getResultAndroid(enums)
            """       
            |   Task.execute {
            |     val emitter =
            |       reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            |       manager.$funName(${getParametersAndroid(enums)}).collect {
            |       emitter.emit("${escapedName}_${funName}_${getParametersSignature()}", ${res.first})
            |     }
            |     promise.resolve(true)
            |   }.onFailure {
            |     promise.reject(it)
            |   }
           """.trimMargin() to res.second
        }
        type.startsWith(PREFIX_TASK_COMMON_FLOW) -> {
            val res = getResultAndroid(enums)
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(type)?.value
            if (commonFlowListClass != null) {
                funNameList = funName.removeSuffix("Flow").plus("ArrayFlow")
            }
            """       
            |   manager.${funNameList ?: funName}(${getParametersAndroid(enums)}).onSuccess { res -> 
            |     val emitter =
            |          reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            |     Task.execute {
            |       res.collect {
            |           emitter.emit("${escapedName}_${funName}_${getParametersSignature()}", ${res.first})
            |       }
            |       promise.resolve(true)
            |     }.onFailure {
            |       promise.reject(it)
            |     }
            |   }.onFailure {
            |     promise.reject(it)
            |   }
           """.trimMargin() to res.second
        }
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultAndroid(enums)
            """       
            |    manager.${simpleName.asString()}(${getParametersAndroid(enums)}).onSuccess {
            |      promise.resolve(${res.first})
            |    }.onFailure {
            |      promise.reject(it)
            |    }
           """.trimMargin() to res.second
        }
        else -> {
            "       try {\n" +
                    "          promise.resolve(manager.${simpleName.asString()}(${
                        getParametersAndroid(
                            enums
                        )
                    })) \n" +
                    "       } catch (e:Exception){\n" +
                    "          promise.reject(e)\n" +
                    "       }\n" to null
        }
    }

}

private fun KSFunctionDeclaration.getMethodBodyIos(
    className: String,
    enums: HashMap<String, List<String>>
): Triple<String, String?, List<String>> {
    val type = returnType?.resolve()?.toString()
    val paramsRes = getParametersIos(enums)
    val funName = simpleName.asString()
    var funNameList: String? = null
    val eventName = "${className}_${funName}_${getParametersSignature()}"
    return when {
        type == null -> Triple("", null, emptyList())
        type.startsWith(CLASS_COMMON_FLOW) -> {
            val res = getResultIos()

            Triple(
                """
            |let manager = ${className}Manager.Companion().getInstanceOrNull()
            |if (manager == nil) {
            |            reject("$funName error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            ${paramsRes.second}
            |            manager!.${funName}(${paramsRes.first}).watch { (result: ${res.second}?, error: ClientException?) in
            |                        if(error != nil){
            |                           return reject("${simpleName.asString()} error", "${simpleName.asString()} error", error?.message ?? "empty message")
            |                        }
            |                        guard let res = result else {
            |                            return
            |                        }
            |                        self.sendEvent(withName: "$eventName", body: ${res.first})
            |                    }
            |           resolve(true)
            |        }
            """.trimMargin(), res.second, listOf(eventName)
            )
        }
        type.startsWith(PREFIX_TASK_COMMON_FLOW) -> {
            val res = getResultIos()
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(type)?.value
            if (commonFlowListClass != null) {
                funNameList = funName.removeSuffix("Flow").plus("ArrayFlow")
            }
            Triple(
                """
            |let manager = ${className}Manager.Companion().getInstanceOrNull()
            |if (manager == nil) {
            |            reject("$funName error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            ${paramsRes.second}
            |            
            |            manager!.${funNameList ?: funName}(${paramsRes.first}).onSuccess { streamResult in
            |                        guard let stream = streamResult else {
            |                            return
            |                        }
            |                        stream.watch { (result: ${res.second}?, error: ClientException?) in
            |                            if(error != nil){
            |                                return
            |                            }
            |                            guard let res = result else {
            |                                return
            |                            }
            |                            self.sendEvent(withName: "$eventName", body: ${res.first})
            |                        }
            |                        resolve(true)
            |                    }
            |                    .onFailure { clientException in
            |                        reject("${simpleName.asString()} error", "${simpleName.asString()} error", clientException.message ?? "${simpleName.asString()} error")
            |                    }
            |        }
            """.trimMargin(), res.second, listOf(eventName)
            )
        }

        type.startsWith(PREFIX_TASK) -> {
            val res = getResultIos()
            Triple(
                """
            |let manager = ${className}Manager.Companion().getInstanceOrNull()
            |if (manager == nil) {
            |            reject("${simpleName.asString()} error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            ${paramsRes.second}
            |            manager!.${simpleName.asString()}(${paramsRes.first})
            |                    .onSuccess { result in
            |                        guard let res = result else {
            |                            return
            |                        }
            |                        resolve(${res.first})
            |                    }
            |                    .onFailure { clientException in
            |                        reject("${simpleName.asString()} error", "${simpleName.asString()} error", clientException.message ?? "${simpleName.asString()} error")
            |                    }
            |        }
            """.trimMargin(), res.second, emptyList()
            )
        }
        else -> {
            Triple(
                """
            |let manager = ${className}Manager.Companion().getInstanceOrNull()
            |if (manager == nil) {
            |            reject("${simpleName.asString()} error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            ${paramsRes.second}
            |            resolve(manager!.${simpleName.asString()}(${paramsRes.first}))
            |        }
            """.trimMargin(), null, emptyList()
            )
        }
    }

}

private fun KSFunctionDeclaration.getMethodBodyJs(
    className: String,
    enums: HashMap<String, List<String>>
): Triple<String, List<String>, Set<String>> {
    val type = returnType?.resolve()?.toString()
    val res = getResultJs()
    val params = getParametersJs(enums)
    val returnList = mutableListOf<String>()
    fun addResultImport(){
        returnList.apply {
            if (!res.second.isNullOrEmpty()
                && res.second?.jsType()?.startsWith("typeof") == true
                && res.first.isNotEmpty()
                && res.first != "data"
            ) {
                add(res.second!!)
            }
        }
    }
//    val imports = mutableSetOf<String>().apply {
//        if (!res.second.isNullOrEmpty()
//            && res.second?.jsType()?.startsWith("typeof") == true
//            && res.first.isNotEmpty()
//            && res.first != "data"
//        ) {
//            add(res.second!!)
//        }
//        addAll(params.second)
//    }
    var funName = simpleName.asString()

    return when {
        type == null -> Triple("void", emptyList(), emptySet())
//        type.startsWith(PREFIX_TASK) -> {
        type.startsWith(PREFIX_TASK_COMMON_FLOW) -> {
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(type)?.value
            val commonFlowArrayClass = REGEX_COMMON_FLOW_ARRAY.find(type)?.value

            returnList.add(CLASS_EMITTER_SUBSCRIPTION)
            returnList.add(commonFlowListClass.jsTypeClass())

            when {
                !commonFlowListClass.isNullOrEmpty() || !commonFlowArrayClass.isNullOrEmpty() -> {
                    Triple(
                        """
                | const eventEmitter = new NativeEventEmitter($className);
                |  let eventListener = eventEmitter.addListener(
                |    '${className}_${funName}_${getParametersSignature()}',
                |    (data: string) => {
                |      stream(${res.first});
                |    }
                |  );
                |  $className.${funName}(${params.first}).catch((e: any) => {
                |    error(e);
                |  }); 
                |  return eventListener;
                |
            """.trimMargin(),
                        returnList.apply { addResultImport() },
                        params.second
                    )
                }
                else -> {
                    Triple(
                        """
                | const eventEmitter = new NativeEventEmitter($className);
                |  let eventListener = eventEmitter.addListener(
                |    '${className}_${funName}_${getParametersSignature()}',
                |    (data: string) => {
                |      stream(${res.first});
                |    }
                |  );
                |  $className.$funName(${params.first}).catch((e: any) => {
                |    error(e);
                |  }); 
                |  return eventListener;
                |
            """.trimMargin(),
                        returnList.apply { addResultImport() },
                        params.second
                    )
                }
            }

        }
        type.startsWith(CLASS_COMMON_FLOW) -> {
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value
            returnList.add(CLASS_EMITTER_SUBSCRIPTION)
            Triple(
                """
                | const eventEmitter = new NativeEventEmitter($className);
                |  let eventListener = eventEmitter.addListener(
                |    '${className}_${funName}_${getParametersSignature()}',
                |    (data: string) => {
                |      stream(${res.first});
                |    }
                |  );
                |  $className.$funName(${params.first}).catch((e: any) => {
                |    error(e);
                |  }); 
                |  return eventListener;
                |
            """.trimMargin(),
                returnList.apply { addResultImport() },
                params.second
            )
        }
        else -> {
            if(type.startsWith(PREFIX_TASK_LIST))
                funName = "${funName}Array"

            val dataString = when (type) {
                "Unit", "Task<Unit>" -> ""
                "Boolean", "Task<Boolean>" -> "data: boolean"
                "Int", "Task<Int>" -> "data: number"
                else -> "data: string"
            }
            val resolveString = if (type == "Unit") "" else res.first
            val returnType = "Promise<${res.second.jsType()}>"
            returnList.add(returnType)
            Triple(
                """
            |return new $returnType((resolve, reject) => {
            |    $className.$funName(${params.first})
            |      .then(($dataString) => {
            |        resolve(${resolveString});
            |      })
            |      .catch((e: any) => {
            |        reject(e);
            |      });
            |})      
            """.trimMargin(),
                returnList.apply { addResultImport() },
                params.second
            )
        }
    }

}

private fun KSTypeReference.getDefault(nullability: Nullability): String {
    return when {
        nullability == Nullability.NULLABLE -> "null"
        "$this" == "Boolean" -> "false"
        "$this" == "Int" -> "0"
        "$this" == "Long" -> "0"
        "$this" == "Float" -> "0"
        "$this" == "String" -> "\"\""
        else -> "\"\""
    }
}

fun KSFunctionDeclaration.getTypedParametersAndroid(
    enums: HashSet<String>,
    keepKotlinType: Boolean = false
): String {
    return parameters.mapNotNull { p ->
        val t = p.type.resolve()
        val nullabilityString = if (keepKotlinType) "" else when (t.nullability) {
            Nullability.NULLABLE -> "?"
            else -> ""
        }

        val paramClass = t.declaration.closestClassDeclaration()
        if (paramClass?.classKind == ClassKind.ENUM_CLASS) {
            enums.add(paramClass.getEnumTypeWithParent())
        }


        val defaultString = if (p.hasDefault) " = ${p.type.getDefault(t.nullability)}" else ""
        p.name?.let { name -> "${name.asString()}: ${p.type.kotlinType(keepKotlinType).first}$nullabilityString$defaultString" }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getTypedParametersIos(enums: HashMap<String,List<String>>): String {
    return parameters.mapNotNull { p ->
        val t = p.type.resolve()
        val paramClass = t.declaration.closestClassDeclaration()
        if (paramClass?.classKind == ClassKind.ENUM_CLASS){
            enums[paramClass.getEnumTypeWithParent()] =
                paramClass.getEnumEntries().map { it.simpleName.asString() }.toList()
        }

        p.name?.let { name -> "${name.asString()}: ${p.type.swiftType().first}" }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getTypedParametersJs(enums: HashMap<String, List<String>>): Pair<String, HashSet<String>> {
    val paramClasses = hashSetOf<String>()

    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val type = p.type.jsType(p.hasDefault)
            val t = p.type.resolve()
            val paramClass = t.declaration.closestClassDeclaration()
            if (paramClass?.classKind == ClassKind.ENUM_CLASS) {
                enums[paramClass.getEnumTypeWithParent()] =
                    paramClass.getEnumEntries().map { it.simpleName.asString() }.toList()
            }
            if (type.startsWith("typeof ")) {
                paramClasses.add(type.removePrefix("typeof "))
            }
            "${name.asString()}: $type"
        }
    }.toMutableList().apply {
        val rt = returnType?.resolve()?.toString() ?: return@apply
        if (rt.startsWith(CLASS_COMMON_FLOW) || rt.startsWith(PREFIX_TASK_COMMON_FLOW)) {
            val commonFlowClass = REGEX_COMMON_FLOW.find(rt)?.value
            val commonFlowListClass = REGEX_COMMON_FLOW_LIST.find(rt)?.value ?: REGEX_COMMON_FLOW_ARRAY.find(rt)?.value
            val commonFlowArrayClass = REGEX_COMMON_FLOW_ARRAY.find(rt)?.value

            val klass = commonFlowListClass ?: commonFlowClass
            klass?.let { k -> paramClasses.add(k) }
            var jsClassType = klass.jsType()
            if (commonFlowListClass != null) {
                jsClassType = "${jsClassType}Array"
            }
            if (commonFlowArrayClass != null) {
                jsClassType = "Array<$jsClassType>"
            }

            add(" stream: (data: $jsClassType) => void")
            add(" error: (err: any) => void")
        }
    }.joinToString(",") to paramClasses
}

private fun KSFunctionDeclaration.getTypedHeaderParametersIos(): String {
    val params = parameters.mapIndexedNotNull { index, p ->
        if (index == 0) simpleName.asString()
        else
            p.name?.asString()
    }.joinToString(":")
    return if (params.isBlank())
        "${simpleName.asString()}:withRejecter:"
    else
        params.plus(":withResolver:withRejecter:")
}

private fun KSFunctionDeclaration.getTypedHeaderParametersIosOC(): String {
    val params = parameters.mapIndexedNotNull { index, p ->
        val param = "(${p.type.objectiveCType()})${p.name?.asString()}"
        if (index == 0)
            "${simpleName.asString()}:$param"
        else
            """
            |
            |                 ${p.name?.asString()}:$param
            """.trimMargin()
    }.joinToString("")
    return if (params.isBlank()) {
        "${simpleName.asString()}:"
            .plus(
                """
                |
                |                 (RCTPromiseResolveBlock)resolve
                |                 withRejecter:(RCTPromiseRejectBlock)reject
                """.trimMargin()
            )
    } else {
        params.plus(
            """
            |
            |                 withResolver:(RCTPromiseResolveBlock)resolve
            |                 withRejecter:(RCTPromiseRejectBlock)reject
            """.trimMargin()
        )
    }

}

fun KSFunctionDeclaration.getParametersAndroid(
    enums: HashSet<String>,
    keepKotlinType: Boolean = false
): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val type = p.type.resolve()
            val kotlinType = p.type.kotlinType(keepKotlinType)
            when {
                kotlinType.second && enums.contains(type.declaration.getEnumTypeWithParent()) -> {
                    val res =
                        "${type.declaration.getEnumTypeWithParent()}.valueOf(${name.asString()})"

                    if (p.hasDefault && type.isMarkedNullable) {
                        "${name.asString()}?.let { $res }"
                    } else {
                        res
                    }
                }
                kotlinType.second -> {
                    "${p.type}.fromJson(${name.asString()})"
                }
                else -> {
                    p.name?.asString()
                }
            }
        }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getParametersIos(enums: HashMap<String, List<String>>): Pair<String, String> {
    val enumSwitch = StringBuilder()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val type = p.type.resolve()
            val iosType = p.type.swiftType()
            val paramIos = when {
                iosType.second &&
                        type.declaration.closestClassDeclaration()?.classKind == ClassKind.ENUM_CLASS
                        && enums.keys.contains(type.getEnumTypeWithParent()) -> {
                    val enumValue = "${name.asString()}Value"
                    val enumEntries = enums[type.getEnumTypeWithParent()] ?: emptyList()
                    var typeClass = type.getEnumTypeWithParent()
                    if (typeClass.endsWith(".Type")) {
                        typeClass = typeClass.replace(".Type", ".Type_")
                    }
                    enumSwitch.appendLine(
                        """
                        |let $enumValue: $typeClass
                        |            switch ${name.asString()} {
                    """.trimMargin()
                    )

                    enumEntries.forEach { e ->
                        enumSwitch.appendLine(
                            """
                        |            case "$e":
                        |                $enumValue = ${typeClass}.${
                                e.lowercase().snakeToLowerCamelCase()
                            }
                    """.trimMargin()
                        )
                    }

                    enumSwitch.appendLine(
                        """
                        |            default:
                        |                $enumValue = ${typeClass}.${
                            enumEntries.first().lowercase().snakeToLowerCamelCase()
                        }
                        |            }
                    """.trimMargin())

                    enumValue
                }
                iosType.second -> {
                    "${p.type}.companion.fromJson(json: ${name.asString()})"
                }
                iosType.first == "Bool" -> "${name.asString()} ? true : false"
                else -> name.asString()
            }
            "${name.asString()}: $paramIos"
        }
    }.joinToString(", ") to enumSwitch.toString()
}

private fun KSFunctionDeclaration.getParametersJs(enums: HashMap<String, List<String>>): Pair<String, Set<String>> {
    val paramClasses = hashSetOf<String>()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val typeString = p.type.toString()
            val paramClass = p.type.resolve().declaration.closestClassDeclaration()

            val jsType = p.type.jsType(p.hasDefault)
            when {
                paramClass?.classKind == ClassKind.ENUM_CLASS && enums.keys.contains(
                    p.type.resolve().getEnumTypeWithParent()
                ) -> {
                    "${name.asString()}.name"
                }
                jsType.startsWith("typeof") -> {
                    paramClasses.add(jsType.removePrefix("typeof "))
                    "${p.type}ToJson(${name.asString()})"
                }
                else -> {
                    name.asString()
                }
            }
        }
    }.joinToString(", ") to paramClasses
}