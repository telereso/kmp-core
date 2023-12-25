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

import groovy.util.Node
import groovy.xml.XmlParser
import org.gradle.configurationcache.extensions.capitalized
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.incremental.createDirectory
import java.text.DecimalFormat
import java.math.RoundingMode

plugins {
    alias(kmpLibs.plugins.android.library)
    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.kotlin.serialization)
    alias(kmpLibs.plugins.kotlinx.kover)
    alias(kmpLibs.plugins.test.logger)
    alias(kmpLibs.plugins.dokka)
    alias(kmpLibs.plugins.telereso.kmp)

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

            // Used when configuring source sets manually for declaring which source sets this one depends on
            // dependsOn("module")

            // Used to remove a source set from documentation, test source sets are suppressed by default
            //suppress.set(false)

            // Use to include or exclude non public members THIS IS DEPRACATED
            // includeNonPublic.set(true)

            /**
             * includeNonPublic is currently deprcated. recommened way to expose private or internal classes and funs is using this approach
             * we define the visbilites we are interersted in.
             * note this will make all private funs or clases or interfaces or val public on the doc level.
             * use suppress annotation to reomve any classes of fun you dont want part of the doc.
             * In our project we have classes with no package. the doc displats them in a root,. by rifght we chsould have a packages for each.
             */
            documentedVisibilities.set(
                setOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC, // Same for both Kotlin and Java
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PRIVATE, // Same for both Kotlin and Java
                    // DokkaConfiguration.Visibility.PROTECTED, // Same for both Kotlin and Java
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.INTERNAL, // Kotlin-specific internal modifier
                    //  DokkaConfiguration.Visibility.PACKAGE, // Java-specific package-private visibility
                )
            )

            // Do not output deprecated members. Applies globally, can be overridden by packageOptions
            skipDeprecated.set(false)

            // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
            reportUndocumented.set(true)

            // Do not create index pages for empty packages
            skipEmptyPackages.set(true)

            // This name will be shown in the final output
            // displayName.set("JVM")

            // Platform used for code analysis. See the "Platforms" section of this readme
            // platform.set(org.jetbrains.dokka.Platform.jvm)


            // Allows to customize documentation generation options on a per-package basis
            // Repeat for multiple packageOptions
            // If multiple packages match the same matchingRegex, the longuest matchingRegex will be used
//            perPackageOption {
//                matchingRegex.set("kotlin($|\\.).*") // will match kotlin and all sub-packages of it
//                // All options are optional, default values are below:
//                skipDeprecated.set(false)
//                reportUndocumented.set(true) // Emit warnings about not documented members
//                includeNonPublic.set(false)
//            }
            // Suppress a package
//            perPackageOption {
//                matchingRegex.set(""".*\.internal.*""") // will match all .internal packages and sub-packages
//                suppress.set(true)
//            }

            // Include generated files in documentation
            // By default Dokka will omit all files in folder named generated that is a child of buildDir
            //  suppressGeneratedFiles.set(false)
        }
    }
}

tasks.register("copyLatestVersionDocs") {
    val docs = rootDir.resolve("public").resolve("docs").resolve("core")
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
    androidTarget {
        publishLibraryVariants("release")
    }
//    tasks.getByName("compileReleaseKotlinAndroid").dependsOn("kspCommonMainKotlinMetadata")


    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "core"
        }
    }

    jvm {
        compilations.all {
//            kotlinOptions.jvmTarget = "1.8"
        }
        //withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    /**
     * Adding JS target to this lib. initially when creating this project, on Android studio the JS option is missing
     * for KKM Library.
     *
     */
    js(IR) {
        moduleName = "@$scope/${project.name}"
        version = project.version as String

        /**
         * browser()
         * It sets the JavaScript target execution environment as browser.
         * It provides a Gradle task—jsBrowserTest that runs all js tests inside the browser using karma and webpack.
         */
        browser {
            testTask {
                useMocha()
            }
        }
        /**
         * nodejs()
         * It sets the JavaScript target execution environment as nodejs.
         * It provides a Gradle task—jsNodeTest that runs all js tests inside nodejs using the built-in test framework.
         */
        nodejs()
        /**
         * binaries.library()
         * It tells the Kotlin compiler to produce Kotlin/JS code as a distributable node library.
         * Depending on which target you've used along with this,
         * you would get Gradle tasks to generate library distribution files
         */
        binaries.library()
        /**
         * binaries.executable()
         * it tells the Kotlin compiler to produce Kotlin/JS code as webpack executable .js files.
         */
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs()

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

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(kmpLibs.bundles.kotlinx)
                /**
                 * Add Ktor dependencies
                 * To use the Ktor client in common code, add the dependency to ktor-client-core to the commonMain
                 */
                implementation(kmpLibs.bundles.ktor)

                implementation(kmpLibs.napier)

                // Multiplatform settings for Shared Preference
                implementation(kmpLibs.multiplatform.settings )

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(kmpLibs.test.kotlinx.coroutines.test)
//                implementation(kmpLibs.test.kotest.framework.engine)
//                implementation(kmpLibs.test.kotest.assertions.core)

                // Ktor Server Mock
                implementation(kmpLibs.test.ktor.client.mock)

                implementation(kmpLibs.test.multiplatform.settings.test)
//                implementation(kmpLibs.test.turbine)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.runtime.jvm)
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.android.driver)
                implementation(kmpLibs.androidx.core)
                implementation(kmpLibs.androidx.lifecycle.process)
            }
        }
        val androidUnitTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        /**
         * By using the by creating scope, we ensure the rest of the Darwin targets
         * pick dependecies from the iOSMain.
         * Note using this actual implementations should only exist in the iosMain else
         * the project will complain.
         */
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                /**
                 * For iOS, we add the ktor-client-darwin dependency
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 */
                implementation(kmpLibs.ktor.client.darwin)

                implementation(kmpLibs.sqldelight.native.driver)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting {
            dependsOn(commonTest)
        }
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            /**
             * TO runs tests for iOS the simulator should not depend on ioSTEst to avoid duplication.
             */
            //iosSimulatorArm64Test.dependsOn(this)
        }

        /**
         * Adding main and test for JS.
         */
        val jsMain by getting {
            dependencies {
                /**
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 */
                implementation(kmpLibs.ktor.client.js)

                implementation(kmpLibs.sqldelight.sqljs.driver)

                implementation(npm("sql.js", kmpLibs.versions.sqlJs.get()))
            }
        }
        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kmpLibs.sqldelight.sqljs.driver)
            }
        }
    }
}

tasks.named<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>("compileKotlinJs").configure {
    dependsOn("jsCleanLibraryDistribution")
    kotlinOptions.moduleKind = "umd"
}

tasks.register<Copy>("copyiOSTestResources") {
    from("${rootDir}/lib/src/commonTest/resources")
    into("${rootDir}/lib/build/bin/iosSimulatorArm64/debugTest/resources")
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
    namespace = "$group.${project.name}"
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
                minValue = 50
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
    from("${buildDir}/reports/tests")
    into("${rootDir}/public/tests/${project.name}/")
}


tasks.register("createCoverageBadge") {
    doLast {

        val report = buildDir.resolve("reports/kover/report.xml")
        val coverage = if (report.exists()) {
            val node = (XmlParser().parse(report)
                .children()
                .first { (it as Node).attribute("type") == "LINE" } as Node)

            val missed = node.attribute("missed").toString().toDouble()
            val covered = node.attribute("covered").toString().toDouble()
            val total = missed + covered
            val coverage = DecimalFormat("#.#").apply {
                roundingMode = RoundingMode.UP
            }.format((covered * 100) / total)
            coverage.toDouble()
        } else {
            null
        }

        val badgeColor = when {
            coverage == null -> "inactive"
            coverage >= 90 -> "brightgreen"
            coverage >= 65 -> "green"
            coverage >= 50 -> "yellowgreen"
            coverage >= 35 -> "yellow"
            coverage >= 20 -> "orange"
            else -> "red"
        }

        val koverDir = rootDir.resolve("public/tests/kover").apply {
            if (!exists())
                createDirectory()
        }
        download(
            "https://img.shields.io/badge/coverage-${coverage?.toString().plus("%25") ?: "unknown"}-$badgeColor",
            koverDir.resolve("badge.svg").path
        )
    }
}

tasks.findByName("koverXmlReport")?.apply {
    finalizedBy("copyTestReportToPublish")
    finalizedBy("createCoverageBadge")
}


fun String.execute(): Process = ProcessGroovyMethods.execute(this)
fun Process.text(): String = ProcessGroovyMethods.getText(this)


fun download(url: String, path: String) {
    val destFile = File(path)
    if (!destFile.exists())
        destFile.createNewFile()
    ant.invokeMethod("get", mapOf("src" to url, "dest" to destFile))
}

tasks.getByName("compileKotlinWasmJs")
    .dependsOn("kspCommonMainKotlinMetadata")

tasks.getByName("wasmJsSourcesJar")
    .dependsOn("kspCommonMainKotlinMetadata")