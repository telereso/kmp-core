plugins {
    `version-catalog`
    id("maven-publish")
    id("convention.publication")
}

group = rootProject.group
version = "0.32-wasm"


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

            // Android
            "agp" to kmpLibs.versions.agp.get(),
            "compileSdk" to kmpLibs.versions.compileSdk.get(),
            "buildTools" to kmpLibs.versions.buildTools.get(),
            "minSdk" to kmpLibs.versions.minSdk.get(),

            //iOS
            "test-iphone-device" to kmpLibs.versions.test.iphone.device.get(),

            // Telereso
            "teleresoCore" to kmpLibs.versions.teleresoCore.get(),
            "teleresoKmp" to kmpLibs.versions.teleresoKmp.get(),

            // Others
            "ktor" to kmpLibs.versions.ktor.get(),
            "okhttp" to kmpLibs.versions.okhttp.get(),
            "napier" to kmpLibs.versions.napier.get(),
            "multiplatformSettings" to kmpLibs.versions.multiplatformSettings.get(),
            "sqldelight" to kmpLibs.versions.sqldelight.get(),
            "buildkonfig" to kmpLibs.versions.buildkonfig.get(),
            "sqlJs" to kmpLibs.versions.sqlJs.get(),
            "js.joda.core" to kmpLibs.versions.js.joda.core.get(),

            // Test
            "kotest" to kmpLibs.versions.kotest.get(),
            "turbine" to kmpLibs.versions.turbine.get(),
            "test-logger" to kmpLibs.versions.test.logger.get()
        )

        versionList.forEach {
            version(it.first, it.second)
        }

        val libList = listOf(
            // kotlin
            Triple("kotlinx.coroutines.core", kmpLibs.kotlinx.coroutines.core.get(), "coroutines"),
            Triple("kotlinx.datetime", kmpLibs.kotlinx.datetime.get(), "datetime"),
            Triple("kotlinx.serialization.json", kmpLibs.kotlinx.serialization.json.get(), "serializationJson"),
            Triple("test.kotlinx.coroutines.test", kmpLibs.test.kotlinx.coroutines.test.get(), "coroutines"),
            Triple("ksp", kmpLibs.ksp.get(), "ksp"),

            // Ktor
            Triple("ktor.client.core", kmpLibs.ktor.client.core.get(), "ktor"),
            Triple("ktor.client.auth", kmpLibs.ktor.client.auth.get(), "ktor"),
            Triple("ktor.client.logging", kmpLibs.ktor.client.logging.get(), "ktor"),
            Triple("ktor.client.content.negotiation", kmpLibs.ktor.client.content.negotiation.get(), "ktor"),
            Triple("ktor.serialization.kotlinx.json", kmpLibs.ktor.serialization.kotlinx.json.get(), "ktor"),
            Triple("ktor.client.darwin", kmpLibs.ktor.client.darwin.get(), "ktor"),
            Triple("ktor.client.js", kmpLibs.ktor.client.js.get(), "ktor"),
            Triple("ktor.client.okhttp", kmpLibs.ktor.client.okhttp.get(), "ktor"),
            Triple("test.ktor.client.mock", kmpLibs.test.ktor.client.mock.get(), "ktor"),

            // Others
            Triple("okhttp.logging", kmpLibs.okhttp.logging.get(), "okhttp"),

            Triple("sqldelight.runtime", kmpLibs.sqldelight.runtime.asProvider().get(), "sqldelight"),
            Triple("sqldelight.runtime.jvm", kmpLibs.sqldelight.runtime.jvm.get(), "sqldelight"),
            Triple("sqldelight.sqlite.driver", kmpLibs.sqldelight.sqlite.driver.get(), "sqldelight"),
            Triple("sqldelight.android.driver", kmpLibs.sqldelight.android.driver.get(), "sqldelight"),
            Triple("sqldelight.native.driver", kmpLibs.sqldelight.native.driver.get(), "sqldelight"),
            Triple("sqldelight.sqljs.driver", kmpLibs.sqldelight.sqljs.driver.get(), "sqldelight"),
            Triple("sqldelight.coroutines.extensions", kmpLibs.sqldelight.coroutines.extensions.get(), "sqldelight"),

            Triple("telereso.core", kmpLibs.telereso.core.asProvider().get(), "teleresoCore"),
            Triple("telereso.core.jvm", kmpLibs.telereso.core.jvm.get(), "teleresoCore"),
            Triple("telereso.core.android", kmpLibs.telereso.core.android.get(), "teleresoCore"),

            Triple("napier", kmpLibs.napier.get(), "napier"),
            Triple("multiplatform.settings", kmpLibs.multiplatform.settings.get(), "multiplatformSettings"),
            Triple("test.multiplatform.settings.test", kmpLibs.test.multiplatform.settings.test.get(), "multiplatformSettings"),

            // Test
            Triple("test.kotest.framework.engine", kmpLibs.test.kotest.framework.engine.get(), "kotest"),
            Triple("test.kotest.assertions.core", kmpLibs.test.kotest.assertions.core.get(), "kotest"),
            Triple("test.turbine", kmpLibs.test.turbine.get(), "turbine")

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
            Triple("kotlinx.kover", kmpLibs.plugins.kotlinx.kover.get(), "kover"),
            Triple("dokka", kmpLibs.plugins.dokka.get(), "dokka"),
            Triple("ksp", kmpLibs.plugins.ksp.get(), "ksp"),

            // Android
            Triple("android.library", kmpLibs.plugins.android.library.get(), "agp"),
            Triple("android.application", kmpLibs.plugins.android.application.get(), "agp"),

            // Telereso
            Triple("telereso.kmp", kmpLibs.plugins.telereso.kmp.get(), "teleresoKmp"),

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
