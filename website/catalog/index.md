---
layout: default
title: Catalog
nav_order: 6
has_children: true
child_nav_order: reversed
---

# Catalog

{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

---

Kotlin multiplatform depends on variant Sdks and libraries for networking and databases ...etc, <br>
keeping track of all them and finding the compatible versions with each other or with Kotlin or Gradle might be tricky,

Therefor this version catalog is created and maintained.

{: .note-title }
> Version Catalog
>
> you can read more about version catalogs [here](https://docs.gradle.org/current/userguide/platforms.html){:target="_blank"} 

# Setup

In your `settings.gradle.kts` add `versionCatalogs` section under `dependencyResolutionManagement`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("kmpLibs") { 
            from("io.telereso.kmp:catalog:0.17")
            // override versions
            // version("teleresoKmp", "0.0.30")
        }
    }
}
```

after performing gradle sync kmpLibs object will be accessible across your submodules

{: .note-title }
> Importing Version Catalog
>
> you can read more about importing version catalog [here](https://docs.gradle.org/current/userguide/platforms.html#sec:importing-published-catalog){:target="_blank"}

### Overriding Versions

Some time we want to test a new feature, fix a bug or downgrade one of the libs so we need to override the catalog version, <br>
As mentioned in the above example you can override a version by providing the version name (alias) followed by the custom version 

```kotlin
versionCatalogs {
    create("kmpLibs") { 
        from("io.telereso.kmp:catalog:0.17")
         version("teleresoKmp", "0.0.30")
         version("ktor", "<new version>")
    }
}
```
# Usage

start migrating your old string dependencies into `kmpLibs` one 

in your project's `build.gradle`

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kmpLibs.multiplatform.settings)
                implementation(kmpLibs.napier)
                implementation(kmpLibs.bundles.kotlinx)
                implementation(kmpLibs.bundles.ktor)
            }
        }
    }
}
```

## Versions

The catalog has compatible sdk and libs versions and that includes java, kotlin and gradle , some configuration require you to get the version only ,

For example setting jvm version

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf ("VERSION_${kmpLibs.versions.java.get()}")
        targetCompatibility = JavaVersion.valueOf ("VERSION_${kmpLibs.versions.java.get()}")
    }
    kotlinOptions {
        jvmTarget = kmpLibs.versions.java.get()
    }
}
```

Notice we need to use `get()` like `kmpLibs.versions.java.get()` as it will return the string version.

## Libs 

Like any other catalog we can implement a library 

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kmpLibs.napier)
            }
        }
    }
}
```

## Test Libs

There are multiple test libraries can be used with kotlin multiplatform , <br>
They are bundled under `test` prefix to avoid adding them to main source code 

```kotlin
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kmpLibs.test.kotlinx.coroutines.test)
                implementation(kmpLibs.test.kotest.framework.engine)
                implementation(kmpLibs.test.kotest.assertions.core)

                implementation(kmpLibs.test.ktor.client.mock)

                implementation(kmpLibs.test.multiplatform.settings.test)
                implementation(kmpLibs.test.turbine)
            }
        }
    }
}
```

## Bundles

Catalog has a useful feature of bundles , where we can add multiple related implementation in one line ,<br>
For example when adding ktor we might add all its sub libraries like so 

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kmpLibs.ktor.client.core)
                implementation(kmpLibs.ktor.client.auth)
                implementation(kmpLibs.ktor.serialization.kotlinx.json)
                implementation(kmpLibs.ktor.serialization.logging)
            }
        }
    }
}
```

or we can use a bundle

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kmpLibs.bundles.ktor)
            }
        }
    }
}
```

## Plugins

Kmp Catalog also support plugins , like dokka and android application ,<br>
In your root build.gradle.kts add the following

```kotlin
plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(kmpLibs.plugins.android.application) apply false
    alias(kmpLibs.plugins.android.library) apply false

    alias(kmpLibs.plugins.kotlin.serialization) apply false
    alias(kmpLibs.plugins.kotlin.parcelize) apply false
    alias(kmpLibs.plugins.kotlin.multiplatform) apply false
    alias(kmpLibs.plugins.kotlin.native.cocoapods) apply false
    alias(kmpLibs.plugins.sqldelight) apply false
}
```