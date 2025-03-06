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
    `version-catalog`
    id("maven-publish")
    id("convention.publication")
}

group = rootProject.group
version = kmpLibs.versions.catalog.get()


catalog {
    versionCatalog {
        val versionList = listOf(
            "java" to kmpLibs.versions.java.get() ,

            // Kotlin
            "kotlin" to kmpLibs.versions.kotlin.get(),
            "kover" to kmpLibs.versions.kover.get(),
            "dokka" to kmpLibs.versions.dokka.get(),
            "coroutines" to kmpLibs.versions.coroutines.get(),
            "datetime" to kmpLibs.versions.datetime.get(),
            "serializationJson" to kmpLibs.versions.serializationJson.get(),
            "ksp" to kmpLibs.versions.ksp.get(),
            "detekt" to kmpLibs.versions.detekt.get(),

            // Compose
            "compose" to kmpLibs.versions.compose.asProvider().get(),
            "compose-navigation" to kmpLibs.versions.compose.navigation.get(),
            "coil3" to kmpLibs.versions.coil3.get(),

            // Android
            "agp" to kmpLibs.versions.agp.get(),
            "compileSdk" to kmpLibs.versions.compileSdk.get(),
            "buildTools" to kmpLibs.versions.buildTools.get(),
            "minSdk" to kmpLibs.versions.minSdk.get(),
            "androidx-lifecycle" to kmpLibs.versions.androidx.lifecycle.get(),
            "androidx-activity-compose" to kmpLibs.versions.androidx.activity.compose.get(),

            //iOS
            "test-iphone-device" to kmpLibs.versions.test.iphone.device.get(),

            // Telereso
            "teleresoCore" to kmpLibs.versions.teleresoCore.get(),
            "teleresoCoreTest" to kmpLibs.versions.teleresoCore.get(),

            // Others
            "ktor" to kmpLibs.versions.ktor.get(),
            "okhttp" to kmpLibs.versions.okhttp.get(),
            "napier" to kmpLibs.versions.napier.get(),
            "multiplatformSettings" to kmpLibs.versions.multiplatformSettings.get(),
            "sqldelight" to kmpLibs.versions.sqldelight.get(),
            "buildkonfig" to kmpLibs.versions.buildkonfig.get(),
            "sqlJs" to kmpLibs.versions.sqlJs.get(),
            "js.joda.core" to kmpLibs.versions.js.joda.core.get(),
            "copy.webpack.plugin" to kmpLibs.versions.copy.webpack.plugin.get(),

            // Test
            "kotest" to kmpLibs.versions.kotest.get(),
            "turbine" to kmpLibs.versions.turbine.get(),
            "test-logger" to kmpLibs.versions.test.logger.get(),
            "mockk" to kmpLibs.versions.mockk.get()
        )

        versionList.forEach {
            version(it.first, it.second)
        }

        val libList = listOf(
            // kotlin
            Triple("kotlinx.coroutines.core", kmpLibs.kotlinx.coroutines.core.get(), "coroutines"),
            Triple("kotlinx-coroutines-swing", kmpLibs.kotlinx.coroutines.swing.get(), "coroutines"),
            Triple("kotlinx.datetime", kmpLibs.kotlinx.datetime.get(), "datetime"),
            Triple("kotlinx.serialization.json", kmpLibs.kotlinx.serialization.json.get(), "serializationJson"),
            Triple("test.kotlinx.coroutines.test", kmpLibs.test.kotlinx.coroutines.test.get(), "coroutines"),
            Triple("ksp", kmpLibs.ksp.get(), "ksp"),

            // Android
            Triple("androidx-activity-compose", kmpLibs.androidx.activity.compose.get(), "androidx-activity-compose"),
            Triple("androidx-lifecycle-viewmodel", kmpLibs.androidx.lifecycle.viewmodel.get(), "androidx-lifecycle"),

            // Ktor
            Triple("ktor.client.core", kmpLibs.ktor.client.core.get(), "ktor"),
            Triple("ktor.client.auth", kmpLibs.ktor.client.auth.get(), "ktor"),
            Triple("ktor.client.logging", kmpLibs.ktor.client.logging.get(), "ktor"),
            Triple("ktor.client.content.negotiation", kmpLibs.ktor.client.content.negotiation.get(), "ktor"),
            Triple("ktor.serialization.kotlinx.json", kmpLibs.ktor.serialization.kotlinx.json.get(), "ktor"),
            Triple("ktor.client.darwin", kmpLibs.ktor.client.darwin.get(), "ktor"),
            Triple("ktor.client.js", kmpLibs.ktor.client.js.get(), "ktor"),
            Triple("ktor.client.okhttp", kmpLibs.ktor.client.okhttp.get(), "ktor"),
            Triple("ktor-client-cio", kmpLibs.ktor.client.cio.get(), "ktor"),
            Triple("ktor-utils", kmpLibs.ktor.utils.get(), "ktor"),
            Triple("ktor-server-swagger", kmpLibs.ktor.server.swagger.get(), "ktor"),
            Triple("test.ktor.client.mock", kmpLibs.test.ktor.client.mock.get(), "ktor"),

            // Compose
            Triple("compose-navigation", kmpLibs.compose.navigation.get(), "compose-navigation"),
            Triple("coil3-compose", kmpLibs.coil3.compose.asProvider().get(), "coil3"),
            Triple("coil3-compose-core", kmpLibs.coil3.compose.core.get(), "coil3"),
            Triple("coil3-network-ktor", kmpLibs.coil3.network.ktor.get(), "coil3"),
            Triple("coil3-svg", kmpLibs.coil3.svg.get(), "coil3"),
            Triple("coil3-mp", kmpLibs.coil3.mp.get(), "coil3"),

            // Others
            Triple("okhttp.logging", kmpLibs.okhttp.logging.get(), "okhttp"),

            Triple("sqldelight.runtime", kmpLibs.sqldelight.runtime.asProvider().get(), "sqldelight"),
            Triple("sqldelight.runtime.jvm", kmpLibs.sqldelight.runtime.jvm.get(), "sqldelight"),
            Triple("sqldelight.sqlite.driver", kmpLibs.sqldelight.sqlite.driver.get(), "sqldelight"),
            Triple("sqldelight.android.driver", kmpLibs.sqldelight.android.driver.get(), "sqldelight"),
            Triple("sqldelight.native.driver", kmpLibs.sqldelight.native.driver.get(), "sqldelight"),
            Triple("sqldelight.web.worker.driver", kmpLibs.sqldelight.web.worker.driver.get(), "sqldelight"),
            Triple("sqldelight.coroutines.extensions", kmpLibs.sqldelight.coroutines.extensions.get(), "sqldelight"),

            Triple("telereso.core", kmpLibs.telereso.core.asProvider().get(), "teleresoCore"),
            Triple("telereso.core.icons", kmpLibs.telereso.core.icons.get(), "teleresoCore"),
            Triple("telereso.core.ui", kmpLibs.telereso.core.ui.get(), "teleresoCore"),
            Triple("telereso.core.jvm", kmpLibs.telereso.core.jvm.get(), "teleresoCore"),
            Triple("telereso.core.android", kmpLibs.telereso.core.android.get(), "teleresoCore"),

            Triple("napier", kmpLibs.napier.get(), "napier"),
            Triple("multiplatform.settings", kmpLibs.multiplatform.settings.asProvider().get(), "multiplatformSettings"),
            Triple("multiplatform.settings.coroutines", kmpLibs.multiplatform.settings.coroutines.get(), "multiplatformSettings"),
            Triple("test.multiplatform.settings.test", kmpLibs.test.multiplatform.settings.test.get(), "multiplatformSettings"),

            // Test
            Triple("test.kotest.framework.engine", kmpLibs.test.kotest.framework.engine.get(), "kotest"),
            Triple("test.kotest.assertions.core", kmpLibs.test.kotest.assertions.core.get(), "kotest"),
            Triple("test.turbine", kmpLibs.test.turbine.get(), "turbine"),
            Triple("test.telereso.core", kmpLibs.test.telereso.core.asProvider().get(), "teleresoCore"),
            Triple("test.telereso.core.ui", kmpLibs.test.telereso.core.ui.get(), "teleresoCore"),
            Triple("test.coil3", kmpLibs.test.coil3.get(), "coil3"),
            Triple("test.mockk", kmpLibs.test.mockk.get(), "mockk"),

            )

        libList.forEach {
            library(it.first,it.second.group,it.second.name).versionRef(it.third)
        }

        bundle(
            "ktor", listOf(
                "ktor-client-core",
                "ktor-client-auth",
                "ktor-client-content-negotiation",
                "ktor-serialization-kotlinx-json",
                "ktor-client-logging",
            )
        )

        bundle(
            "coil3", listOf(
                "coil3-compose",
                "coil3-compose-core",
                "coil3-network-ktor",
                "coil3-svg",
                "coil3-mp"
            )
        )

        bundle(
            "kotlinx", listOf(
                "kotlinx-coroutines-core", "kotlinx-datetime", "kotlinx-serialization-json"
            )
        )

        bundle(
            "sqldelight", listOf(
                "sqldelight-runtime", "sqldelight-coroutines-extensions"
            )
        )

        bundle(
            "test.kotest", listOf(
                "test-kotest-framework-engine", "test-kotest-assertions-core"
            )
        )

        val pluginList = listOf(

            // Kotlin
            Triple("kotlin.android", kmpLibs.plugins.kotlin.android.get(), "kotlin"),
            Triple("kotlin.jvm", kmpLibs.plugins.kotlin.jvm.get(), "kotlin"),
            Triple("kotlin.serialization", kmpLibs.plugins.kotlin.serialization.get(), "kotlin"),
            Triple("kotlin.parcelize", kmpLibs.plugins.kotlin.parcelize.get(), "kotlin"),
            Triple("kotlin.multiplatform", kmpLibs.plugins.kotlin.multiplatform.get(), "kotlin"),
            Triple("kotlin.native.cocoapods", kmpLibs.plugins.kotlin.native.cocoapods.get(), "kotlin"),
            Triple("compose-compiler", kmpLibs.plugins.compose.compiler.get(), "kotlin"),
            Triple("kotlinx.kover", kmpLibs.plugins.kotlinx.kover.get(), "kover"),
            Triple("dokka", kmpLibs.plugins.dokka.get(), "dokka"),
            Triple("ksp", kmpLibs.plugins.ksp.get(), "ksp"),
            Triple("detekt", kmpLibs.plugins.detekt.get(), "detekt"),
            Triple("compose", kmpLibs.plugins.compose.asProvider().get(), "compose"),

            // Android
            Triple("android.library", kmpLibs.plugins.android.library.get(), "agp"),
            Triple("android.application", kmpLibs.plugins.android.application.get(), "agp"),

            // Telereso
            Triple("telereso.kmp", kmpLibs.plugins.telereso.kmp.get(), "teleresoCore"),

            // Test
            Triple("test-logger", kmpLibs.plugins.test.logger.get(), "test-logger"),

            // Others
            Triple("buildkonfig", kmpLibs.plugins.buildkonfig.get(), "buildkonfig"),
            Triple("sqldelight", kmpLibs.plugins.sqldelight.get(), "sqldelight")
        )

        pluginList.forEach {
            plugin(it.first, it.second.pluginId).versionRef(it.third)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}