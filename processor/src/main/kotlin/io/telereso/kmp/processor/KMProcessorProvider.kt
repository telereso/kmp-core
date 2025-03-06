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

package io.telereso.kmp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import io.telereso.kmp.annotations.Builder
import io.telereso.kmp.annotations.FlutterExport
import io.telereso.kmp.annotations.ListWrappers
import io.telereso.kmp.annotations.ReactNativeExport
import io.telereso.kmp.annotations.SwiftOverloads
import io.telereso.kmp.processor.builder.BuilderSymbolValidator
import io.telereso.kmp.processor.builder.BuilderVisitor
import io.telereso.kmp.processor.flutter.FlutterModelSymbolValidator
import io.telereso.kmp.processor.flutter.FlutterModelVisitor
import io.telereso.kmp.processor.lists.ListWrapperVisitor
import io.telereso.kmp.processor.lists.ListWrappersSymbolValidator
import io.telereso.kmp.processor.model.ModelSymbolValidator
import io.telereso.kmp.processor.model.ModelVisitor
import io.telereso.kmp.processor.reactnative.ReactNativeMangerVisitor
import io.telereso.kmp.processor.reactnative.ReactNativeSymbolValidator
import io.telereso.kmp.processor.swift.SwiftSymbolValidator
import io.telereso.kmp.processor.swift.SwiftVisitor

class KMProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KMPModelProcessor(
            environment,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
            scope = environment.options["scope"],
            packageName = environment.options["packageName"]
        )
    }
}

class KMPModelProcessor(
    private val environment: SymbolProcessorEnvironment,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val scope: String? = null,
    private val packageName: String? = null,
) : SymbolProcessor {
    private val flutterValidator = FlutterModelSymbolValidator(logger)
    private val modelValidator = ModelSymbolValidator(logger)
    private val reactNativeValidator = ReactNativeSymbolValidator(logger)
    private val builderValidator = BuilderSymbolValidator(logger)
    private val listWrapperValidator = ListWrappersSymbolValidator(logger)
    private val swiftSymbolValidator = SwiftSymbolValidator(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("KMPModelProcessor was invoked.")

        return processModel(resolver, packageName) +
                processFlutter(resolver, packageName) +
                processReactNative(resolver, scope, packageName) +
                processBuilder(resolver, packageName) +
                processListWrappers(resolver, packageName) +
                processSwiftOverLoad(environment, resolver,SwiftOverloads::class.qualifiedName, packageName) +
                processSwiftOverLoad(environment, resolver,JvmOverloads::class.qualifiedName, packageName)
    }

    private fun processModel(resolver: Resolver, packageName: String?): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = kotlinx.serialization.Serializable::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter {
                it.validate()
                true
            }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor = ModelVisitor(codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                modelValidator.isValid(it)
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processFlutter(resolver: Resolver, packageName: String?): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = FlutterExport::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter { it.validate() }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor = FlutterModelVisitor(codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                flutterValidator.isValid(it)
                true
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processReactNative(
        resolver: Resolver,
        scope: String?,
        packageName: String?
    ): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = ReactNativeExport::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter { it.validate() }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor =
                ReactNativeMangerVisitor(logger, codeGenerator, dependencies, scope, packageName)

            validatedSymbols.filter {
                reactNativeValidator.isValid(it)
                true
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processBuilder(resolver: Resolver, packageName: String?): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = Builder::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter {
                it.validate()
                true
            }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor = BuilderVisitor(codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                builderValidator.isValid(it)
                true
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processListWrappers(
        resolver: Resolver,
        packageName: String?
    ): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = ListWrappers::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter {
                it.validate()
                true
            }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor =
                ListWrapperVisitor(codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                listWrapperValidator.isValid(it)
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processSwiftOverLoad(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        annotationName:String?,
        packageName: String?
    ): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        if (annotationName == JvmOverloads::class.qualifiedName
            && !environment.options["swiftOverloadsByJvmOverloads"].toBoolean()
        ) return unresolvedSymbols

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter {
                it.validate()
            }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor =
                SwiftVisitor(environment,codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                swiftSymbolValidator.isValid(it)
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }
}