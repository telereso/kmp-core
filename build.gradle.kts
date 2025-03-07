import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(kmpLibs.plugins.android.application) apply false
    alias(kmpLibs.plugins.android.library) apply false

    alias(kmpLibs.plugins.kotlin.serialization) apply false
    alias(kmpLibs.plugins.kotlin.parcelize) apply false
    alias(kmpLibs.plugins.kotlin.multiplatform) apply false
    alias(kmpLibs.plugins.kotlin.native.cocoapods) apply false
    alias(kmpLibs.plugins.sqldelight) apply false
    alias(kmpLibs.plugins.detekt) apply false
    alias(kmpLibs.plugins.compose) apply false
    alias(kmpLibs.plugins.compose.compiler) apply false

}

group = "io.telereso.kmp"
version = project.findProperty("publishVersion") ?: "0.0.1-local"
val scope by extra { "telereso" }


buildscript {
    repositories {
        mavenCentral()
        google()
    }
}
//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

tasks.register("detektAll") {
    allprojects {
        this@register.dependsOn(tasks.withType<Detekt>())
    }
}