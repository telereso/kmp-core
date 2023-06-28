pluginManagement {
    val localProps = mutableMapOf<String, String>()
    File("local.properties").apply {
        if (exists()) useLines { lines ->
            lines.forEach {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) localProps[parts[0].trim()] = parts[1].trim()
            }
        }
    }

    val teleresoKmpVersion: String by settings
    val kmpVersion = localProps.getOrDefault("teleresoKmpVersion", teleresoKmpVersion)
    plugins {
        id("io.telereso.kmp") version kmpVersion apply false
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
    }
}

rootProject.name = "core"
includeBuild("convention-plugins")
include(":lib")
project(":lib").name = rootProject.name
include(":androidApp")
include(":jvmApi")

