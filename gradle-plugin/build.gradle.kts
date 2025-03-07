import java.util.*

plugins {
    `kotlin-dsl` // Is needed to turn our build logic written in Kotlin into Gralde Plugin
    `java-gradle-plugin`
    alias(kmpLibs.plugins.ksp)
    id("com.gradle.plugin-publish") version "1.2.1"
    signing
}

buildscript {

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kmpLibs.versions.kotlin.get()}")
    }
}


repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

group = rootProject.group
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
    targetCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = kmpLibs.versions.java.get()
    }
}

gradlePlugin {
    description =
        "Include tasks needed while working with Telereso's Kotlin multiplatform annotations also to support react native and flutter"
    website.set("https://kmp.telereso.io/annotations/")
    vcsUrl.set("https://github.com/telereso/kmp-annotations")

    plugins {
        create("kmp") {
            id = "io.telereso.kmp"
            displayName = "Kotlin multiplatform plugin"
            description =
                "Include tasks needed while working with Telereso's Kotlin multiplatform annotations also to support react native and flutter"
            implementationClass = "io.telereso.kmp.KmpPlugin"
            tags.set(listOf("kotlin", "Kotlin Multiplatform", "kmm", "kmp", "Telereso", "ReactNative"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("${project.rootProject.layout.buildDirectory}/.m2/repository")
        }
    }
}


dependencies {
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${kmpLibs.versions.ksp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kmpLibs.versions.kotlin.get()}")
}