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

import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.incremental.createDirectory

plugins {
    alias(kmpLibs.plugins.android.library)
    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.kotlin.serialization)
    alias(kmpLibs.plugins.kotlinx.kover)
    alias(kmpLibs.plugins.test.logger)
    alias(kmpLibs.plugins.dokka)
    alias(kmpLibs.plugins.telereso.kmp)
    alias(kmpLibs.plugins.compose)
    alias(kmpLibs.plugins.compose.compiler)

    id("maven-publish")
    id("convention.publication")
}

group = rootProject.group
version = rootProject.version
val scope: String by rootProject.extra

/**
 * https://kotlin.github.io/dokka/1.6.0/user_guide/gradle/usage/
 */
tasks.dokkaHtml.configure {
    moduleName.set(rootProject.name.split("-").joinToString(" ") { it.capitalized() })

    outputDirectory.set(
        rootDir.resolve("public/docs/${project.name}/${rootProject.version}")
    )

    dokkaSourceSets {
        configureEach { // Or source set name, for single-platform the default source sets are `main` and `test`
            documentedVisibilities.set(
                setOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC, // Same for both Kotlin and Java
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PRIVATE, // Same for both Kotlin and Java
                    // DokkaConfiguration.Visibility.PROTECTED, // Same for both Kotlin and Java
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.INTERNAL, // Kotlin-specific internal modifier
                    //  DokkaConfiguration.Visibility.PACKAGE, // Java-specific package-private visibility
                )
            )
            skipDeprecated.set(false)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
        }
    }
}

tasks.register("copyLatestVersionDocs") {
    val docs = rootDir.resolve("public").resolve("docs").resolve("core-ui")
    doFirst {
        delete {
            delete(docs.resolve("latest"))
        }
    }

    doLast {
        copy {
            from(docs.resolve(rootProject.version.toString()))
            into(docs.resolve("latest"))
        }
    }
}

tasks.getByName("dokkaHtml").finalizedBy("copyLatestVersionDocs")

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = kmpLibs.versions.java.get()
            }
        }
    }

    js {
        moduleName = "teleresoIcons"
        version = project.version as String

        browser()
        //binaries.library()
        binaries.executable()
        generateTypeScriptDefinitions()
    }

    wasmJs {
        moduleName = "teleresoIcons"
        browser()
        binaries.executable()
    }

    sourceSets {

        /**
         * https://kotlinlang.org/docs/opt-in-requirements.html#module-wide-opt-in
         * If you don't want to annotate every usage of APIs that require opt-in,
         * you can opt in to them for your whole module.
         * To opt in to using an API in a module, compile it with the argument -opt-in,
         * specifying the fully qualified name of the opt-in requirement annotation of the API you use
         */
        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.components.resources)
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.telereso.kmp.core.icons.resources"
    generateResClass = always
}

tasks.register<Copy>("copyiOSTestResources") {
    from("${projectDir}/src/commonTest/resources")
    into("${projectDir}/build/bin/iosSimulatorArm64/debugTest/resources")
}
tasks.findByName("iosSimulatorArm64Test")?.dependsOn("copyiOSTestResources")

tasks.named(
    "iosSimulatorArm64Test",
    org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java
).configure {
    device = kmpLibs.versions.test.iphone.device.get()
}

tasks.named(
    "iosX64Test",
    org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java
).configure {
    device = kmpLibs.versions.test.iphone.device.get()
}



android {
    namespace = "$group.core.icons"
    compileSdk = kmpLibs.versions.compileSdk.get().toInt()
    buildFeatures {
        compose = true
        buildConfig = false
    }
    defaultConfig {
        minSdk = kmpLibs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
    }
    kotlin {
        androidTarget {
            compilations.all {
                kotlinOptions {
                    jvmTarget = kmpLibs.versions.java.get()
                }
            }
        }
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

// We can filter out some classes in the generated report
koverReport {
    filters {
        excludes {
            classes(listOf("*.*Test*"))
        }
    }
    // The koverVerify currently only supports line counter values.
    // we can also configure this to run after the unit tests task.
    verify {
        // Add VMs in the includes [list]. VMs added,their coverage % will be tracked.
        filters {
            excludes {
                classes(listOf("*.*Test*"))
            }
        }
        // Enforce Test Coverage
        rule("Minimal line coverage rate in percent") {
            bound {
                minValue = 0
            }
        }
    }

    defaults {
        html {
            setReportDir(rootDir.resolve("public/tests/kover"))
        }
    }
}

testlogger {

    theme =
        com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA // pick a theme - mocha, standard or plain
    showExceptions = true // show detailed failure logs
    showStackTraces = true
    showFullStackTraces =
        false // shows full exception stack traces,  useful to see the entirety of the stack trace.
    showCauses = true

    /**
     * sets threshold in milliseconds to highlight slow tests,
     * any tests that take longer than 0.5 seconds to run would have their durations logged using a warning style
     * and those that take longer than 1 seconds to run using an error style.
     */
    slowThreshold = 1000

    showSummary =
        true // displays a breakdown of passes, failures and skips along with total duration
    showSimpleNames = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showOnlySlow = false
    /**
     * filter the log output based on the type of the test result.
     */
    showStandardStreams = true
    showPassedStandardStreams = true
    showSkippedStandardStreams = true
    showFailedStandardStreams = true

    logLevel = LogLevel.LIFECYCLE
}

tasks.register<Copy>("copyTestReportToPublish") {
    from("${layout.buildDirectory}/reports/tests")
    into("${rootDir}/public/tests/${project.name}/")
}


tasks.findByName("jsBrowserProductionLibraryDistribution")
    ?.dependsOn("jsProductionExecutableCompileSync")
tasks.findByName("jsNodeProductionLibraryDistribution")
    ?.dependsOn("jsProductionExecutableCompileSync")
tasks.findByName("compileKotlinDesktop")?.dependsOn("kspCommonMainKotlinMetadata")
tasks.findByName("jsBrowserProductionWebpack")
    ?.dependsOn("wasmJsProductionExecutableCompileSync")
tasks.findByName("wasmJsBrowserProductionWebpack")
    ?.dependsOn("jsProductionExecutableCompileSync")


tasks.create("processMaterialIcons") {
    val repoPath = project.properties["repoPath"]?.toString() ?: ""
    enabled = repoPath.isNotBlank()

    doLast {
        val drawableDir = projectDir.resolve("src/commonMain/composeResources/drawable")
        val previewPage =
            project(":core-preview").projectDir.resolve("src/commonMain/kotlin/io/telereso/kmp/core/preview/pages/SymbolsPreviewPageHelper.g.kt")
        drawableDir.deleteRecursively()
        drawableDir.createDirectory()
        var counter = 0
        val allParameters = mutableListOf<String>()

        file(repoPath).resolve("android").listFiles()?.forEach { group ->
            group.listFiles()?.forEach { iconDir ->
                iconDir.resolve("materialicons/black/res/drawable").listFiles()?.forEach {
                    if (it.name.endsWith("_24.xml")) {
                        counter++
                        copy {
                            from(it.path)
                            into(drawableDir)
                            rename { name ->
                                val finalName = name
                                    .removePrefix("baseline_")
                                    .snakeToPascal()
                                    .let { after  ->
                                        when {
                                            after.startsWith("class") -> after.replace("class", "_class")
                                            after.startsWith("try") -> after.replace("try", "_try")
                                            after.first().isDigit() -> "_$after"
                                            else -> after
                                        }
                                    }
                                    .replace("24.xml", ".xml")

                                allParameters.add(finalName.split(".")[0])

                                finalName
                            }
                        }
                    }
                }
            }
        }
        drawableDir.listFiles()?.forEach { drawableFile ->
            drawableFile.writeText(
                drawableFile.readText()
                    .replace("@android:color/white", "#FFFFFF")
                    .replace("    ", " ")
                    .replace("\n", "")
            )
        }
        layout.buildDirectory.file("iconNames.txt").get().asFile.apply {
            if (exists())
                delete()
            createNewFile()
            writeText(allParameters.joinToString())
        }

        previewPage.apply {
            if (exists())
                delete()
            createNewFile()
        }
        previewPage.appendText(
            """
            |package io.telereso.kmp.core.preview.pages
            |
            |import io.telereso.kmp.core.icons.MaterialIcons
            |import io.telereso.kmp.core.icons.resources.*
            |
            |val iconMap = mapOf(
            |    ${allParameters.joinToString("\n    ") { "\"$it\" to MaterialIcons.$it," }}
            |)
        """.trimMargin()
        )
        println("processed $counter vector")
    }
}

fun String.snakeToPascal(): String {
    return this.split("_")
        .joinToString("") { it.capitalize() }
}