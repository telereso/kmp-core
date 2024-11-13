import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.incremental.createDirectory

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

plugins {
    alias(kmpLibs.plugins.android.library)
    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.compose)
    alias(kmpLibs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = kmpLibs.versions.java.get()
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "TeleresoCorePreview"
            isStatic = true
        }
    }

    jvm("desktop")

    wasmJs {
        moduleName = "teleresoCorePreview"
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here

//            implementation(project(":core"))
//            implementation(project(":core-ui"))
//            implementation(project(":core-icons"))
            implementation("io.telereso.kmp:core:0.5.1")
            implementation("io.telereso.kmp:core-icons:0.5.1")
            implementation("io.telereso.kmp:core-ui:0.5.1")

            implementation(kmpLibs.kotlin.reflect)

            implementation(kmpLibs.bundles.kotlinx)
            implementation(kmpLibs.bundles.coil3)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(kmpLibs.compose.navigation)
            implementation(kmpLibs.androidx.lifecycle.viewmodel)

            implementation("io.telereso.kmp:jsontree:2.4.0")
            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc09")
        }
        commonTest.dependencies {

        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "io.telereso.kmp.core.preview.resources"
    generateResClass = auto
}


android {
    namespace = "io.telereso.kmp.core.preview"
    compileSdk = kmpLibs.versions.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = false
    }
    defaultConfig {
        minSdk = kmpLibs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
    }
}


compose.desktop {
    application {
        mainClass = "io.telereso.kmp.core.preview.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TeleresoCorePreview"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("../common/src/desktopMain/resources/images")
            macOS {
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                menuGroup = "Compose Examples"
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "18159995-d967-4CD2-8885-77BFA97CFA9F"
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }

        }

        buildTypes.release.proguard {
            configurationFiles.from("rules.pro")
        }
    }
}

tasks.register("processMaterialSymbols") {
    val repoPath = project.properties["repoPath"]?.toString() ?: ""
    enabled = repoPath.isNotBlank()

    doLast {
        val drawableDir = projectDir.resolve("src/commonMain/composeResources/drawable")
        drawableDir.deleteRecursively()
        drawableDir.createDirectory()

        val modelsDir = rootDir.resolve("core-ui/src/commonMain/kotlin/io.telereso.kmp.core.ui/models/")
        val symbolsClass = modelsDir.resolve("Symbols.kt")

        fun setupClass(file: File) {
            if (file.exists())
                file.delete()
            file.apply {
                createNewFile()
                appendText("package io.telereso.kmp.core.ui.models")
                appendText("\n\n")
                appendText("import io.telereso.kmp.core.ui.models.Symbols")
                appendText("\n\n")
            }
        }

        setupClass(symbolsClass)

        var counter = 0
        val allParameters = mutableListOf<String>()
        file(repoPath).resolve("symbols/android").listFiles()?.forEach { iconDir ->
            fun processFile(type: String, file: File, createSymbolsClass: Boolean = false) {
                if (!file.name.contains("grad") &&
                    !file.name.contains("wght") &&
                    file.name.endsWith("_24px.xml")
                ) {
                    counter++
                    val isFill = file.name.contains("fill1_")
//                copy {
//                    from(file.path)
//                    into(drawableDir)
//                    rename { name ->
//                        type + "_${if (isFill) "filled_" else ""}" + name
//                            .replace("fill1_", "")
//                            .replace("_24px.xml", ".xml")
//                            .let { after ->
//                                when {
//                                    after.startsWith("class") -> after.replace("class", "_class")
//                                    after.startsWith("try") -> after.replace("try", "_try")
//                                    else -> after
//                                }
//                            }
//                    }
//                }


                    if (createSymbolsClass && !isFill) {
                        val parameterName = iconDir.name.snakeToPascal().let {
                            if (it.first().isDigit()) "_${it}" else it
                        }
                        allParameters.add(iconDir.name)

                        symbolsClass.appendText("val Symbols.${parameterName} get() = SymbolConfig(\"${iconDir.name}\")\n\n")
                    }
                }
            }
            iconDir.resolve("materialsymbolsoutlined").listFiles()?.forEach {
                processFile("outlined", it, true)
            }
            iconDir.resolve("materialsymbolsrounded").listFiles()?.forEach {
                processFile("rounded", it)
            }
            iconDir.resolve("materialsymbolssharp").listFiles()?.forEach {
                processFile("sharp", it)
            }
        }

        layout.buildDirectory.file("symbolsNames.txt").get().asFile.apply {
            if (exists())
                delete()
            createNewFile()
            writeText(allParameters.joinToString())
        }

        drawableDir.listFiles()?.forEach { drawableFile ->
            drawableFile.writeText(
                drawableFile.readText()
                    .replace("@android:color/white", "#FFFFFF")
                    .replace("    ", " ")
                    .replace("\n", "")
            )
        }
        println("processed $counter vector")
    }
}

fun String.snakeToPascal(): String {
    return this.split("_")
        .joinToString("") { it.capitalize() }
}