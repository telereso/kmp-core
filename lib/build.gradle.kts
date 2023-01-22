import org.gradle.configurationcache.extensions.capitalized

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.dokka")
    id("io.telereso.kmp")
    id("convention.publication")
}


// Setup extras variables
val ktorVersion: String by rootProject.extra
val sqlDelightVersion: String by rootProject.extra
val coroutinesVersion: String by rootProject.extra
val napierVersion: String by rootProject.extra
val kotlinxDatetimeVersion: String by rootProject.extra
val buildToolsVersion: String by rootProject.extra
val minSdkVersion: Int by rootProject.extra
val compileSdkVer: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra
val multiplatformSettingsVersion: String by rootProject.extra

// Setup publish variables
val baseProjectName = rootProject.name.replace("-client", "")
project.ext["artifactName"] = "${baseProjectName}-${project.name}"

group = rootProject.group
version = rootProject.version

publishing {
    repositories {
        maven {
            url = uri("${project.findProperty("artifactoryUrl") ?: "test"}/mobile-gradle")
            credentials {
                username = (project.findProperty("artifactoryUser") ?: "test") as String
                password = (project.findProperty("artifactoryPassword") ?: "test") as String
            }
        }
    }
}

//apply(from = "publish.gradle")

/**
 * https://kotlin.github.io/dokka/1.6.0/user_guide/gradle/usage/
 */
tasks.dokkaHtml.configure {
    moduleName.set(rootProject.name.split("-").joinToString(" ") { it.capitalized() })

    outputDirectory.set(
        rootDir.resolve(
            "public${
                project.findProperty("publishVersion")?.let { "/$it" } ?: ""
            }"
        )
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

kotlin {
    android {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
        moduleName = "core-client"

        compilations["main"].packageJson {
            customField("buildTimeStamp", "${System.currentTimeMillis()}")
        }
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
            dependencies {
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                // Kotlin Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")


                /**
                 * Add Ktor dependencies
                 * To use the Ktor client in common code, add the dependency to ktor-client-core to the commonMain
                 */
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")

                implementation("io.github.aakira:napier:$napierVersion")

                // Multiplatform settings for Shared Preference
                implementation("com.russhwolf:multiplatform-settings-no-arg:$multiplatformSettingsVersion")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation("io.kotest:kotest-framework-engine:5.5.3")
                implementation("io.kotest:kotest-assertions-core:5.5.3")
                /**
                 * currently mockk is not support for Kotlin/Native iOS
                 * leaving these hear for easier reference later/.
                 */
//                 implementation("io.mockk:mockk-common:1.12.4")
//                 implementation("io.mockk:mockk:1.13.2")

                // Ktor Server Mock
                implementation("io.ktor:ktor-client-mock:$ktorVersion")

                implementation("com.russhwolf:multiplatform-settings-test:1.0.0-RC")
            }
        }
        val jvmMain by getting {
            dependencies {
                /**
                 * Add a ktor engine dependency
                 * For Android, you can also use other engine types.
                 * https://ktor.io/docs/http-client-engines.html#jvm-android
                 * We chose the okhttp engine for Android since its one we are
                 * most familiar with.
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 *
                 */
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                /**
                 * For logging using the okHttp but we could also use the Ktor logging
                 * implementation("io.ktor:ktor-client-logging:2.0.0-beta-1")
                 */
                implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

                // sqlDelight jVM version
                implementation("com.squareup.sqldelight:runtime-jvm:1.5.4")
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            /**
             * Since Android tests run on the JVM. we need to implement the database for it as well
             */
            dependencies {
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.4")
            }
        }
        val androidMain by getting {
            dependencies {
                /**
                 * Add a ktor engine dependency
                 * For Android, you can also use other engine types.
                 * https://ktor.io/docs/http-client-engines.html#jvm-android
                 * We chose the okhttp engine for Android since its one we are
                 * most familiar with.
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 *
                 */
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
                /**
                 * For logging using the okHttp but we could also use the Ktor logging
                 * implementation("io.ktor:ktor-client-logging:2.0.0-beta-1")
                 */
                implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
            }
        }
        val androidTest by getting {
            dependsOn(commonTest)
            dependencies {
                /**
                 * In some tests (like verification of migrations) you might wish to swap out the Android driver with the JVM driver,
                 * enabling you to test code involving the database without needing an Android emulator or physical device. To do that use the jvm SQLite driver:
                 * https://cashapp.github.io/sqldelight/android_sqlite/testing/
                 */
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.4")
                implementation("io.mockk:mockk:1.13.2")
                implementation("io.mockk:mockk-common:1.12.4")
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
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")

                implementation("com.squareup.sqldelight:native-driver:$sqlDelightVersion")

                /**
                 * For some reason iOS cannot pick up this implementation from commonMain.
                 * Had to add it here. Maybw later we can look into this
                 */
                // implementation("io.github.aakira:napier:$napierVersion")
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
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation("com.squareup.sqldelight:sqljs-driver:$sqlDelightVersion")

//                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                implementation(npm("sql.js", "1.7.0"))
            }
        }
        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation("com.squareup.sqldelight:sqljs-driver:$sqlDelightVersion")
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

tasks.named("iosSimulatorArm64Test", org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java
).configure {
    deviceId = "iPhone 14 Pro"
}

tasks.named("iosX64Test", org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java).configure {
    deviceId = "iPhone 14 Pro"
}



android {
    namespace = "$group.${project.name}"
    compileSdk = compileSdkVer
    buildFeatures {
        buildConfig = false
    }
    defaultConfig {
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
    }
}

// We can filter out some classes in the generated report
kover {
    filters {
        classes {
            //includes += listOf("*.*ViewModelImpl*", "io.telereso.kmp.core.cache.*")
            // exclude any classes named with Test
            excludes += listOf("*.*Test*")
        }
    }
    // The koverVerify currently only supports line counter values.
    // we can also configure this to run after the unit tests task.
    verify {
        // Add VMs in the includes [list]. VMs added,their coverage % will be tracked.
        filters {
            classes {
                //includes += listOf("*.*ViewModelImpl*", "io.telereso.kmp.core.cache.*")
                excludes += listOf("*.*Test*")
            }
        }
       // Enforce Test Coverage
       rule {
           name = "Minimal line coverage rate in percent"
           bound {
               minValue = 50
           }
       }
    }

    // We can configure the test results index.html to be stored anywhere within our propejct. normally its generated in the build folder
//    htmlReport {
//        reportDir.set(File("testresults"))
//    }
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
