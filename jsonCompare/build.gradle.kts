import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.android.library)
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
            baseName = "JsonCompare"
            isStatic = true
        }
    }

    jvm("desktop")

//    wasmJs {
//        moduleName = "teleresoJsonCompare"
//        browser()
//        binaries.executable()
//    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here

            implementation(project(":core"))
            implementation(project(":core-ui"))

            implementation(kmpLibs.kotlin.reflect)

            implementation(kmpLibs.bundles.kotlinx)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(kmpLibs.compose.navigation)
            implementation(kmpLibs.androidx.lifecycle.viewmodel)

            implementation("io.telereso.kmp:jsontree:2.4.0-alpha.1")
            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc09")
        }
        commonTest.dependencies {

        }
    }
}

android {
    namespace = "io.telereso.kmp.jsonCompare"
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
        mainClass = "io.telereso.kmp.jsoncompare.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TeleresoJsonCompare"
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