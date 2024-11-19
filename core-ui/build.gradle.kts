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
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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


// Setup publish variables
val baseProjectName = rootProject.name.replace("-client", "")
project.ext["artifactName"] = "${baseProjectName}-${project.name}"

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

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "TeleresoUI"
            linkerOpts += "-lsqlite3"
            isStatic = false
            export(project(":core"))
        }
    }

    jvm()

    js {
        moduleName = "teleresoUI"
        version = project.version as String

        browser()
        //binaries.library()
        binaries.executable()
        generateTypeScriptDefinitions()
    }

    wasmJs {
        moduleName = "teleresoUI"
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
                api(project(":core"))

                implementation(kmpLibs.bundles.kotlinx)
                /**
                 * Add Ktor dependencies
                 * To use the Ktor client in common code, add the dependency to ktor-client-core to the commonMain
                 */
                implementation(kmpLibs.bundles.ktor)

                implementation(kmpLibs.napier)

                // Multiplatform settings for Shared Preference
                implementation(kmpLibs.multiplatform.settings)

                // Multiplatform settings for observing and collecting settings flows
                implementation(kmpLibs.multiplatform.settings.coroutines)

                implementation(kmpLibs.sqldelight.runtime)

                implementation(kmpLibs.coil3.compose.core)
                implementation(kmpLibs.coil3.compose)
                implementation(kmpLibs.coil3.mp)
                implementation(kmpLibs.coil3.network.ktor)
                implementation(kmpLibs.coil3.svg)

                implementation(compose.runtime)
                implementation(compose.foundation)
                api(compose.ui)
                api(compose.material3)
                api(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(kmpLibs.compose.navigation)
                implementation(kmpLibs.androidx.lifecycle.viewmodel)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(kmpLibs.test.kotlinx.coroutines.test)
                implementation(kmpLibs.test.kotest.framework.engine)
                implementation(kmpLibs.test.kotest.assertions.core)

                // Ktor Server Mock
                implementation(kmpLibs.test.ktor.client.mock)

                implementation(kmpLibs.test.multiplatform.settings.test)
                implementation(kmpLibs.test.turbine)
            }
        }

        androidMain {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.android.driver)
                implementation(kmpLibs.androidx.core)
                implementation(kmpLibs.androidx.lifecycle.process)

                implementation(compose.preview)
                api(kmpLibs.androidx.activity.compose)
//                implementation(kmpLibs.compose.webview)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }

        val noneAndroidMain by creating {
            dependsOn(commonMain.get())

            dependencies {

            }
        }

        jvmMain {
            dependsOn(noneAndroidMain)
            dependencies {

                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.runtime.jvm)
                implementation(kmpLibs.sqldelight.sqlite.driver)

                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)
                implementation(kmpLibs.kotlinx.coroutines.swing)
                implementation(kmpLibs.ktor.client.cio)

//                api(kmpLibs.compose.webview)
            }
        }

        jvmTest {
            dependencies {
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }

        iosMain {
            dependsOn(noneAndroidMain)
            dependencies {
                /**
                 * For iOS, we add the ktor-client-darwin dependency
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 */
                implementation(kmpLibs.ktor.client.darwin)

                implementation(kmpLibs.sqldelight.native.driver)
//                implementation(kmpLibs.compose.webview)
            }
        }

        val jsWasmMain by creating {
            dependsOn(noneAndroidMain)
            dependsOn(commonMain.get())

            dependencies {
                implementation(kmpLibs.ktor.client.js)

                implementation(kmpLibs.sqldelight.web.worker.driver)
                implementation(devNpm("copy-webpack-plugin", kmpLibs.versions.copy.webpack.plugin.get()))
                implementation(npm("sql.js", kmpLibs.versions.sqlJs.get()))
            }
        }
        jsMain.get().dependsOn(jsWasmMain)
        wasmJsMain.get().dependsOn(jsWasmMain)
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "io.telereso.kmp.core.ui.resources"
    generateResClass = auto
}

compose.desktop {
    application {
        mainClass = "io.telereso.kmp.core.ui.preview.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "CoreUIPreview"
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

//tasks.named<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>("compileKotlinJs").configure {
//    dependsOn("jsCleanLibraryDistribution")
//    kotlinOptions.moduleKind = "umd"
//}

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
    namespace = "$group.core.ui"
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
