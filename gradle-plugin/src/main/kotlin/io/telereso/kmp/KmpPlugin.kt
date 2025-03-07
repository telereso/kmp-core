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

package io.telereso.kmp

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import io.telereso.kmp.TeleresoKmpExtension.Companion.teleresoKmp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.newInstance
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

const val KEY_TELERESO_KMP_DEVELOPMENT_MODE = "teleresoKmpDevelopmentPath"
const val KEY_TELERESO_KMP_VERSION = "teleresoKmpVersion"
const val KEY_TELERESO_CATALOG_NAME = "teleresoCatalogName"


class KmpPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {

        pluginManager.apply(KspGradleSubplugin::class.java)

        val kspExtension = extensions.getByType(KspExtension::class.java)
        val scope = getScope()?.let { scope ->
            kspExtension.arg("scope", scope)
            scope
        }

        val localProps = Properties().apply {
            File("${rootProject.rootDir}/local.properties").apply {
                if (exists())
                    inputStream().use { fis ->
                        load(fis)
                    }
            }
        }

        val catalogNameLocalProp = localProps[KEY_TELERESO_CATALOG_NAME]?.toString()
        val catalogNameProjectProp = findProperty(KEY_TELERESO_CATALOG_NAME)?.toString()
        val libs = extensions.findByType(VersionCatalogsExtension::class.java)
            ?.named(catalogNameLocalProp ?: catalogNameProjectProp ?: "kmpLibs")


        val devModeLocalProp = localProps[KEY_TELERESO_KMP_DEVELOPMENT_MODE]?.toString()
        val devModeProjectProp = findProperty(KEY_TELERESO_KMP_DEVELOPMENT_MODE)?.toString()

        if (!(devModeLocalProp ?: devModeProjectProp).isNullOrBlank()
            && findProperty("publishGradlePlugin")?.toString()?.toBoolean() != true
        ) {
            log("Using local projects :annotations and :processor")
            dependencies.add("commonMainImplementation", project(":annotations"))
            dependencies.add("kspCommonMainMetadata", project(":processor"))
        } else {
            val annotationsVersion = libs?.findVersion("teleresoCore")?.getOrNull() ?: findProperty(KEY_TELERESO_KMP_VERSION)?.toString()
            dependencies.add("commonMainImplementation", "io.telereso.kmp:annotations:$annotationsVersion")
            dependencies.add("kspCommonMainMetadata", "io.telereso.kmp:processor:$annotationsVersion")
        }

        val teleresoKmp = teleresoKmp()


        fun includeGeneratedClassesToSrcSet(kotlinMultiplatformExtension: KotlinMultiplatformExtension?) {
            kotlinMultiplatformExtension
                ?.sourceSets
                ?.findByName("commonMain")
                ?.kotlin {
                    srcDirs(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
                }
        }

        afterEvaluate {
            kspExtension.arg(
                "swiftOverloadsByJvmOverloads",
                teleresoKmp.swiftOverloadsByJvmOverloads.toString()
            )
            kspExtension.arg("createObjectFunctionName", teleresoKmp.createObjectFunctionName)

            val kotlinMultiplatformExtension = project.extensions
                .findByType(KotlinMultiplatformExtension::class.java)

            if (teleresoKmp.enableReactNativeExport || !teleresoKmp.disableJsonConverters) {
                includeGeneratedClassesToSrcSet(kotlinMultiplatformExtension)
            }

            // Common tasks


            val jsCleanLibraryDistributionTask = "jsCleanLibraryDistribution"
            tasks.create<Delete>(jsCleanLibraryDistributionTask) {
                group = "Clean"
                delete(layout.buildDirectory.dir("dist/js/productionLibrary"))
            }

            tasks.findByName("compileKotlinJs")?.apply {
                dependsOn(jsCleanLibraryDistributionTask)
            }

            val dependsOnTasks = listOf(
                "compileKotlinJs",
                "compileKotlinJvm",
                "compileCommonMainKotlinMetadata",
                "compileDebugKotlinAndroid",
                "compileKotlinIosArm64",
                "compileKotlinIosSimulatorArm64",
                "compileKotlinIosX64",
                "jsBrowserProductionLibraryDistribution",
                "preBuild",
                "build",
                "allTests"
            )

            tasks.findByName("compileReleaseKotlinAndroid")
                ?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileDebugKotlinAndroid")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinJvm")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinIosArm64")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinIosSimulatorArm64")
                ?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinIosX64")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinJs")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("jsSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("iosArm64SourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("sourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("iosX64SourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("iosSimulatorArm64SourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("jvmSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")


            tasks.findByName("jsProductionExecutableCompileSync")?.let {
                tasks.findByName("jsNodeProductionLibraryPrepare")?.dependsOn(it)
                tasks.findByName("jsBrowserProductionLibraryPrepare")?.dependsOn(it)
                tasks.findByName("jsNodeProductionLibraryDistribution")?.dependsOn(it)
                tasks.findByName("jsBrowserProductionLibraryDistribution")?.dependsOn(it)
            }

            tasks.findByName("jsProductionLibraryCompileSync")?.let {
                tasks.findByName("jsBrowserProductionWebpack")?.dependsOn(it)
                tasks.findByName("jsBrowserProductionLibraryDistribution")?.dependsOn(it)
                tasks.findByName("jsNodeProductionLibraryDistribution")?.dependsOn(it)
            }

            if (teleresoKmp.disableJsonConverters) {
                log("Skipping adding models tasks")
            } else {
                log("Adding Models Tasks")
                includeGeneratedClassesToSrcSet(kotlinMultiplatformExtension)
            }


            val projectPackageName = getProjectName()
            val baseDir = "$rootDir".split("/react-native")[0]

            log("Creating Lib Tasks for project $projectPackageName ${scope?.let { "with scope $it" }}")

            // Android
            val copyGeneratedFilesAndroidTask =
                "kspCommonMainKotlinMetadataCopyGeneratedAndroid"

            val cleanAndroidGeneratedFiles = "cleanAndroidGeneratedFiles"
            tasks.create<Delete>(cleanAndroidGeneratedFiles) {
                group = "Clean"
                delete(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/rn-kotlin"))
            }

            tasks.create<Copy>(copyGeneratedFilesAndroidTask) {
                log("Copying ksp generated reactNative android files")

                from("${layout.buildDirectory}/generated/ksp/metadata/commonMain/resources/rn-kotlin/")
                into(
                    "${baseDir}/react-native-${
                        projectPackageName.replace(
                            ".",
                            "-"
                        )
                    }/android/src/main/java/"
                )

            }

            // iOS
            val copyGeneratedFilesIosTask = "kspCommonMainKotlinMetadataCopyGeneratedIos"

            tasks.create<Copy>(copyGeneratedFilesIosTask) {
                log("Copying ksp generated reactNative ios files")

                from("${layout.buildDirectory.asFile.get().path}/generated/ksp/metadata/commonMain/resources/ios")
                into(
                    "${baseDir}/react-native-${
                        projectPackageName.replace(
                            ".",
                            "-"
                        )
                    }/ios/"
                )
            }

            // Js
            val copyGeneratedFilesJsTask = "kspCommonMainKotlinMetadataCopyGeneratedJs"

            tasks.create<Copy>(copyGeneratedFilesJsTask) {
                log("Copying ksp generated reactNative js files")

                from("${layout.buildDirectory.asFile.get().path}/generated/ksp/metadata/commonMain/resources/js/")
                into("${baseDir}/react-native-${projectPackageName.replace(".", "-")}/src/")
            }

            if (!teleresoKmp.enableReactNativeExport) {
                log("Skipping adding reactNative tasks")
            } else {
                log("Adding reactNative tasks")
                val reactNativeDir = "${baseDir}/react-native-${projectPackageName.replace(".", "-")}"

                // Android tasks
                tasks.getByName(copyGeneratedFilesAndroidTask)
                    .dependsOn("kspCommonMainKotlinMetadata")
//                tasks.getByName(copyGeneratedFilesAndroidTask)
//                    .finalizedBy(cleanAndroidGeneratedFiles)

                val copyAndroidExampleGradle = "copyAndroidExampleGradle"
                tasks.create(copyAndroidExampleGradle) {
                    copy {
                        from("${baseDir}/gradle/")
                        into("$reactNativeDir/example/android/gradle/")
                    }

                    copy {
                        from("${baseDir}/local.properties")
                        into("${reactNativeDir}/example/android/")
                    }
                }

                // iOS tasks
                tasks.getByName(copyGeneratedFilesIosTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                // Js tasks
                tasks.getByName(copyGeneratedFilesJsTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                // Workaround to support gradle 8 and java 17 with kotlin 1.8
                val reactNativeGradle8Workaround = "reactNativeGradle8Workaround"
                tasks.create(reactNativeGradle8Workaround) {
                    listOf(
                        rootDir.resolve(
                            "${baseDir}/react-native-${
                                projectPackageName.replace(
                                    ".",
                                    "-"
                                )
                            }/example/node_modules/react-native-gradle-plugin/build.gradle.kts"
                        ),
                        rootDir.resolve(
                            "${baseDir}/react-native-${
                                projectPackageName.replace(
                                    ".",
                                    "-"
                                )
                            }/node_modules/react-native-gradle-plugin/build.gradle.kts"
                        )
                    ).forEach { f ->
                        if (f.exists()) {
                            log("Applying $reactNativeGradle8Workaround on: ")
                            val content = f.readText()
                                .replace(
                                    """kotlin("jvm") version "1.6.10"""",
                                    """kotlin("jvm") version "1.9.10""""
                                ).replace(
                                    "JavaVersion.VERSION_11",
                                    "JavaVersion.VERSION_17"
                                ).replace(
                                    "JavaVersion.VERSION_1_8",
                                    "JavaVersion.VERSION_17"
                                )
                            f.writeText(content)
                        }
                    }
                }

                val exportReactNativePackages = "exportReactNativePackages"
                val cocoapods = kotlinMultiplatformExtension?.getCocoapods()
                exportReactNativePackages(exportReactNativePackages, teleresoKmp, cocoapods)

                dependsOnTasks.forEach {
                    tasks.findByName(it)?.dependsOn(cleanAndroidGeneratedFiles)
                    tasks.findByName(it)?.dependsOn(copyAndroidExampleGradle)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesAndroidTask)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesIosTask)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesJsTask)
                    if (!teleresoKmp.disableReactNativeGradle8Workaround)
                        tasks.findByName(it)?.dependsOn(reactNativeGradle8Workaround)
                }
            }

            if (teleresoKmp.enableScreenShots)
                screenShotTest(teleresoKmp.screenShotsTolerance)
        }

        gradle.projectsEvaluated {
            tasks.findByName("androidReleaseSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("androidDebugSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
        }
    }


    interface Injected {
        @get:Inject
        val fs: FileSystemOperations
    }

    private fun Project.screenShotTest(tolerance: Float) {
        val screenShotsBuildDir = project.layout.buildDirectory.dir("telereso/screenShots")
        val screenShotsReportDir = project.layout.buildDirectory.dir("telereso/reports/screenShotTest")
        val screenShotsConfig = screenShotsBuildDir.get().file("config.json")
        screenShotsConfig.asFile.apply {
            if (exists()){
                delete()
            }
            ensureParentDirsCreated()
            createNewFile()
            writeText("""
                { 
                  "tolerance": "$tolerance"
                }
            """.trimIndent())
        }


        tasks.register("recordTeleresoScreenShots") {
            finalizedBy("jvmTest") // Ensure jvmTest runs after recodeScreenShots
            val injected = project.objects.newInstance<Injected>()
            val screenShotsDir = projectDir
                .resolve("telereso")
                .resolve("screenShots")

            doFirst {
                injected.fs.delete {
                    delete(screenShotsDir)
                }
            }
        }


        tasks.register("cleanTeleresoScreenShotsReport") {
            val injected = project.objects.newInstance<Injected>()

            doFirst {
                injected.fs.delete {
                    delete(screenShotsReportDir.get())
                }
                injected.fs.delete {
                    delete(screenShotsBuildDir.get().dir("diff"))
                }
            }
        }

        tasks.register("checkTeleresoScreenShotsReport") {
            doLast {
                val reportDir = screenShotsReportDir.get().asFile
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

                fun generateDiscoveryPage(indexFile: File, parentDir: File) {
                    // Get all immediate child directories (direct subdirectories of parentDir)
                    val subdirectories = getDirectoriesRecursive(parentDir).filter { it.parentFile == parentDir }

                    // Create links for each direct subdirectory
                    val links = subdirectories.map { dir ->
                        val dirName = dir.name
                        """<li><a href="${dir.relativeTo(parentDir).path}/index.html">$dirName</a></li>"""
                    }.joinToString("\n")

                    // Define total and passed counts (example values; replace with actual data if needed)
//                    val totalTests = subdirectories.size
//                    val passedTests = totalTests // Assume all passed for now (replace with logic to calculate actual passes)
                    val failedTests = subdirectories.size // Replace with logic if needed
//                    val skippedTests = 0 // Replace with logic if needed

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
                    background-color: #f9f9f9;
                    color: #333;
                }
                h1 {
                    text-align: center;
                    color: #2c3e50;
                }
                .container {
                    max-width: 900px;
                    margin: 0 auto;
                    background: #fff;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    padding: 20px;
                }
                .summary {
                    margin-bottom: 20px;
                    border: 1px solid #ddd;
                    border-radius: 8px;
                    overflow: hidden;
                }
                .summary-header {
                    background-color: #3498db;
                    color: #fff;
                    padding: 10px;
                    font-size: 18px;
                    font-weight: bold;
                }
                .summary-table {
                    width: 100%;
                    border-collapse: collapse;
                }
                .summary-table th, .summary-table td {
                    padding: 10px;
                    text-align: left;
                    border: 1px solid #ddd;
                }
                .summary-table th {
                    background-color: #ecf0f1;
                }
                .status-pass {
                    color: #27ae60;
                    font-weight: bold;
                }
                .status-fail {
                    color: #e74c3c;
                    font-weight: bold;
                }
                .test-list {
                    list-style-type: none;
                    padding: 0;
                }
                .test-list li {
                    margin: 10px 0;
                    padding: 10px;
                    border: 1px solid #ddd;
                    border-radius: 8px;
                    background: #f4f4f4;
                    transition: background 0.3s;
                }
                .test-list li:hover {
                    background: #eaf2f8;
                }
                a {
                    text-decoration: none;
                    color: #3498db;
                    font-weight: bold;
                }
                a:hover {
                    text-decoration: underline;
                }
                footer {
                    text-align: center;
                    margin-top: 20px;
                    font-size: 14px;
                    color: #999;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Test Report: ${parentDir.name}</h1>
                
                <div class="summary">
                    <div class="summary-header">Test Summary</div>
                    <table class="summary-table">
                        <tr>
                            <th>Failed</th>
                            <td class="status-fail">$failedTests</td>
                        </tr>
                    </table>
                </div>

                <ul class="test-list">
                    $links
                </ul>

                <footer>
                    <p>&copy; 2024 Test Report</p>
                </footer>
            </div>
        </body>
        </html>
    """.trimIndent()

                    // Write the content to index.html file in the parent directory
                    indexFile.writeText(content)
                }


                if (reportDir.exists()) {
                    // Get all directories under baseDir recursively
                    val directories = getDirectoriesRecursive((reportDir))

                    directories.forEach { dir ->
                        val indexFile = dir.toPath().resolve("index.html").toFile()

                        // If an index.html already exists in the directory, skip this directory
                        if (indexFile.exists()) {
                            return@forEach
                        }

                        // Generate a discovery page if the directory does not already have index.html
                        generateDiscoveryPage(indexFile, dir)
                    }

                    println("ScreenShot testing failed \uD83D\uDEA8 See the report \uD83D\uDCD5 at: file://${reportDir.path}/index.html")
                }
            }
        }

        tasks.findByName("jvmTest")
            ?.dependsOn("cleanTeleresoScreenShotsReport")
            ?.finalizedBy("checkTeleresoScreenShotsReport")
    }

    private fun Project.exportReactNativePackages(
        exportReactNativePackagesTask: String,
        teleresoKmp: TeleresoKmpExtension,
        cocoapodsExtension: CocoapodsExtension?
    ) {
        log("Processing ReactNative packages: ${teleresoKmp.exportedReactNativePackages.joinToString { name }}")

        var taskEnabled = teleresoKmp.exportedReactNativePackages.isNotEmpty()
                && cocoapodsExtension != null
                && !project.plugins.hasPlugin("com.android.application")

        val rnpDir = teleresoKmp.reactNativePackageDirectory
            ?: rootDir.resolve("react-native-${project.name}")
        if (!rnpDir.exists()) {
            log("React Native package not found!, set it using `reactNativePackageDirectory` or rename dir to `react-native-${project.name}`, caused by setting packages ${teleresoKmp.exportedReactNativePackages}")
            taskEnabled = false
        }


        val frameworkName = teleresoKmp.umbrellaFrameworkName ?: cocoapodsExtension?.name

        log("Detect frameworkName: $frameworkName, if not correct try setting `umbrellaFrameworkName`")

        val nodeModulesDir =
            teleresoKmp.nodeModulesDirectory ?: rnpDir.resolve("node_modules")

        val exportedDir = rnpDir.resolve("ios/Exported")

        tasks.create(exportReactNativePackagesTask) {
            enabled = taskEnabled
            doFirst {
                if (cocoapodsExtension == null) {
                    log("Failed to locate CocoaPods for this project, make sure to implement it first before exporting packages")
                }
                if (exportedDir.exists()) exportedDir.deleteRecursively()
                exportedDir.mkdir()

                teleresoKmp.exportedReactNativePackages.forEach { rnp ->
                    val rnpFolder = nodeModulesDir.resolve(rnp.path)
                    if (!rnpFolder.exists()) throw RuntimeException("exported package not found $rnp at path $rnpFolder")

                    val iosDir = nodeModulesDir.resolve(rnp.pathIos)

                    val exportedPackageDir =
                        exportedDir.resolve(rnp.name.removePrefix("react-native").toPascal())
                    exportedPackageDir.mkdir()

                    copy {
                        from(iosDir.path)
                        into(exportedPackageDir.path)
                        eachFile {
                            if (relativePath.segments.size > 1) {
                                exclude()
                            }
                            if (path.contains(".xcodeproj")
                                || path.contains(".xcworkspace")
                                || (!name.endsWith(".h")
                                        && !name.contains(".m")
                                        && !name.contains(".swift"))
                            ) {
                                exclude()
                            }
                        }

                    }
                }
            }

            doLast {
                project.fileTree(exportedDir).visit {
                    if (isDirectory && file.listFiles().isNullOrEmpty()
                        || file.name.endsWith(".xcodeproj")
                        || file.name.endsWith(".xcworkspace")
                    ) {
                        file.deleteRecursively()
                    }
                }
            }
        }

        val replaceImportedReactNativeFramework = "replaceImportedReactNativeFramework"
        tasks.create(replaceImportedReactNativeFramework) {
            enabled = taskEnabled
            doLast {
                project.fileTree(exportedDir).visit {
                    if (file.name.endsWith(".swift")) {
                        var content = file.readText()

                        if (teleresoKmp.removeStringErrorExtension)
                            content = content.replace(
                                "extension String: Error {\n}", ""
                            )

                        val detectedFrameworkName =
                            """@objc\(([^)]+)\)""".toRegex().find(content)?.groups?.get(1)?.value

                        teleresoKmp.exportedReactNativePackages.forEach { rnp ->
                            (rnp.framework ?: detectedFrameworkName)?.let { oldFramework ->
                                content = content.replace(
                                    "import $oldFramework",
                                    "import $frameworkName"
                                )
                            }
                        }
                        file.writeText(content)
                    }
                }
            }
        }

        tasks.getByName(exportReactNativePackagesTask)
            .dependsOn("kspCommonMainKotlinMetadata")
            .finalizedBy(replaceImportedReactNativeFramework)
    }
}

private fun KotlinMultiplatformExtension.getCocoapods(): CocoapodsExtension? {
    return (this as? org.gradle.api.plugins.ExtensionAware)?.extensions?.findByName("cocoapods") as? CocoapodsExtension
}


fun Project.getProjectName(): String {
    val extraKey = "projectPackageName"
    return when {
        extra.has(extraKey) -> extra.get(extraKey)
        rootProject.extra.has(extraKey) -> rootProject.extra.get(extraKey)
        else -> {
            log("`projectPackageName` was not found in project's extras will use project name instead ")
            name
        }
    }.toString()
}

fun Project.getScope(): String? {
    val extraKey = "scope"
    return when {
        extra.has(extraKey) -> extra.get(extraKey)
        rootProject.extra.has(extraKey) -> rootProject.extra.get(extraKey)
        else -> {
            log("`scope` was not found in project's extras, no scope will be added for js imports")
            null
        }
    }?.toString()
}

fun Project.log(message: String) {
    println("Telereso:$name: $message")
}

fun log(message: String) {
    println("Telereso: $message")
}