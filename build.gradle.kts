plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.3.1").apply(false)
    id("com.android.library").version("7.3.1").apply(false)

    id("org.jetbrains.kotlin.android").version("1.7.21").apply(false)
    id ("org.jetbrains.kotlin.plugin.parcelize").version("1.7.20").apply(false)
    kotlin("multiplatform").version("1.7.21").apply(false)
    id("org.jetbrains.kotlin.native.cocoapods").version("1.7.22").apply(false)

    id("com.squareup.sqldelight").version("1.5.3").apply(false)
    id("io.telereso.kmp").version("0.0.4").apply(false)
}


allprojects {
    ext {
        set("minSdkVersions",21)
        set("ktorVersions","2.1.0")
    }
}


group = "io.telereso.kmp"
version = project.findProperty("publishVersion") ?: "0.0.1"


buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.4")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.4.21")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release")
    }
    // extra.apply {
    //     set("minSdkVersions", 21)
    // }
}

// Android
val buildToolsVersion by extra { "31.0.0" }
val minSdkVersion by extra { 21 }
val compileSdkVer by extra { 31 }
val targetSdkVersion by extra { 31 }

// Dependencies versions
val ktorVersion by extra { "2.1.3" }
val sqlDelightVersion by extra { "1.5.4" }
val coroutinesVersion by extra { "1.6.4" }
val napierVersion by extra { "2.6.1" }
val multiplatformSettingsVersion by extra { "1.0.0-RC" }
val kotlinxDatetimeVersion by extra { "0.4.0" }

allprojects {
    ext {
        set("buildToolsVersion", "31.0.0")
        set("minSdkVersion", 21)
        set("compileSdkVersion", 31)
        set("targetSdkVersion", 31)


        if (System.getProperty("os.arch") == "aarch64") {
            // For M1 Users we need to use the NDK 24 which added support for aarch64
            set("ndkVersion", "24.0.8215888")
        } else {
            // Otherwise we default to the side-by-side NDK version from AGP.
            set("ndkVersion", "21.4.7075529")
        }
        set("reactNativeAndroidRoot", File("${rootProject.rootDir}/react-native-core-client"))
    }
}
//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}