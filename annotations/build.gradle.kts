plugins {
  alias(kmpLibs.plugins.kotlin.multiplatform)
  alias(kmpLibs.plugins.android.library)
  id("maven-publish")
  id("convention.publication")
}

group = rootProject.group
version = rootProject.version

kotlin {

  jvm {
    compilations.all {
      kotlinOptions {
        jvmTarget = kmpLibs.versions.java.get()
      }
    }
  }

  androidTarget()

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  watchosArm32()
  watchosArm64()
  watchosSimulatorArm64()

  js {
    browser()
    nodejs()
    binaries.library()
    binaries.executable()
  }

  sourceSets {

    all {
      languageSettings.optIn("kotlin.js.ExperimentalJsExport")
    }
  }
}

android {
  namespace = "io.telereso.kmp.annotations"
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
  kotlin {
    jvmToolchain(kmpLibs.versions.java.get().toInt())
    androidTarget {
      compilations.all {
        kotlinOptions {
          jvmTarget = kmpLibs.versions.java.get()
        }
      }
    }
  }
}

//////////////////// FIXME  ////////////////////
tasks.getByName("jsBrowserProductionWebpack").dependsOn("jsProductionLibraryCompileSync")
tasks.getByName("jsBrowserProductionLibraryDistribution").dependsOn("jsProductionExecutableCompileSync")
tasks.getByName("jsNodeProductionLibraryDistribution").dependsOn("jsProductionExecutableCompileSync")
//////////////////////////////////////////////////////////////////////////////////////////