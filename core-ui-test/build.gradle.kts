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
import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    alias(kmpLibs.plugins.android.library)
    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.kotlin.serialization)
    alias(kmpLibs.plugins.test.logger)
    alias(kmpLibs.plugins.dokka)
    alias(kmpLibs.plugins.telereso.kmp)
    alias(kmpLibs.plugins.kotlinx.kover)
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
        }
    }
}

tasks.register("copyLatestVersionDocs") {
    val docs = rootDir.resolve("public").resolve("docs").resolve("core-test")
    doFirst {
        delete {
            delete(docs.resolve("latest-ui-test"))
        }
    }

    doLast {
        copy {
            from(docs.resolve(rootProject.version.toString()))
            into(docs.resolve("latest-ui-test"))
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

    jvm()

    js {
        browser()
    }

    wasmJs {
        browser()
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

        androidMain {
            dependencies {
                implementation(kmpLibs.test.mockk)
            }
        }

        commonMain {
            dependencies {
                implementation(project(":core-test"))
                implementation(project(":core-ui"))

                implementation(kmpLibs.bundles.ktor)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)

                // test
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation("com.soywiz:korlibs-image:6.0.0")

            }
        }
    }
}

tasks.named<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>("compileKotlinJs").configure {
    dependsOn("jsCleanLibraryDistribution")
    kotlinOptions.moduleKind = "umd"
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
    namespace = "$group.${project.name.replace("-", ".")}"
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

tasks.findByName("androidDebugSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")

interface Injected {
    @get:Inject
    val fs: FileSystemOperations
}
tasks.register("recodeScreenShots") {
    finalizedBy("jvmTest") // Ensure jvmTest runs after recodeScreenShots
    val screenShotsDir = projectDir.resolve("src").resolve("screenShots")
    val injected = project.objects.newInstance<Injected>()

    doFirst {
        injected.fs.delete {
            delete(screenShotsDir)
        }
    }
}

tasks.register("cleanScreenShots") {
    val screenShotsDir = project.layout.buildDirectory.dir("telereso/screenShots")
    val screenShotsReportDir = project.layout.buildDirectory.dir("telereso/reports/screenShotTest")
    val injected = project.objects.newInstance<Injected>()

    doFirst {
        injected.fs.delete {
            delete(screenShotsDir)
            delete(screenShotsReportDir)
        }
    }
}

tasks.register("checkReport") {
    val screenShotsReportDir = project.layout.buildDirectory.file("telereso/reports/screenShotTest")

    doLast {
        if (screenShotsReportDir.isPresent) {
            // Get all directories under baseDir recursively
            val directories = getDirectoriesRecursive((screenShotsReportDir.get().asFile))

            directories.forEach { dir ->
                val indexFile = dir.toPath().resolve("index.html").toFile()

                // If an index.html already exists in the directory, skip this directory
                if (indexFile.exists()) {
                    return@forEach
                }

                // Generate a discovery page if the directory does not already have index.html
                generateDiscoveryPage(indexFile, dir)
            }

            println("ScreenShot testing failed \uD83D\uDEA8 See the report \uD83D\uDCD5 at: file://${screenShotsReportDir.get().asFile.path}/index.html")
        }


    }
}

tasks.named("jvmTest") {
    dependsOn("cleanScreenShots")
    finalizedBy("checkReport")
}

// Method to recursively get all subdirectories under the base directory
fun getDirectoriesRecursive(rootDir: File): List<File> {
    val directories = mutableListOf<File>()
    rootDir.walkTopDown().forEach { dir ->
        if (dir.isDirectory) {
            directories.add(dir)
        }
    }
    return directories
}

// Method to generate the discovery page for each directory
fun generateDiscoveryPage(indexFile: File, parentDir: File) {
    // Get all immediate child directories (direct subdirectories of parentDir)
    val subdirectories = getDirectoriesRecursive(parentDir).filter { it.parentFile == parentDir }

    val links = subdirectories.map { dir ->
        // Create a link for each direct subdirectory found
        val dirName = dir.name
        """<li><a href="${dir.relativeTo(parentDir).path}/index.html">$dirName</a></li>"""
    }.joinToString("\n")

    // HTML template for the discovery page
    val content = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${parentDir.name}</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        background-color: #f4f4f4;
                    }
                    h1 {
                        text-align: center;
                        color: #333;
                    }
                    .test-title {
                        font-size: 24px;
                        margin-bottom: 15px;
                        text-align: center;
                    }
                    .test-list {
                        list-style-type: none;
                        padding: 0;
                    }
                    .test-list li {
                        margin: 10px 0;
                    }
                    a {
                        text-decoration: none;
                        color: #3498db;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    .divider {
                        margin: 20px 0;
                        border-top: 1px solid #ccc;
                    }
                </style>
            </head>
            <body>
                <div class="test-title">
                    <h2>${parentDir.name}</h2>
                </div>

                <ul class="test-list">
                    $links
                </ul>

                <div class="divider"></div>

                <footer style="text-align: center;">
                    <p>&copy; 2024 Test Report</p>
                </footer>
            </body>
            </html>
        """.trimIndent()

    // Write the content to index.html file in the parent directory
    indexFile.writeText(content)
}