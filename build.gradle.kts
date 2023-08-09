plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.native.cocoapods) apply false
    alias(libs.plugins.sqldelight) apply false
}

group = "io.telereso.kmp"
version = project.findProperty("publishVersion") ?: "0.0.1"


buildscript {
    repositories {
        mavenCentral()
        google()
    }
}
//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}