---
layout: default
title: Annotations
nav_order: 4
has_children: true
---

# Annotations

Telereso plugin offers some useful annotations to support the [kmp structure](../starter/#structure){:target="_blank"}
,

Some of this annotations can be used in other project structures too like [@Builder](Builder.html)

---

# Repo

For any issues or ideas you can check the repo [kmp-annotations](https://github.com/telereso/kmp-annotations){:target="_blank"}

---

# Setup

To be able to use Telereso annotations you need to add
the [kmp plugin](https://plugins.gradle.org/plugin/io.telereso.kmp)

#### Latest version
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/telereso/kmp-annotations)](https://github.com/telereso/kmp-annotations/releases){:target="_blank"}

**gradle.properties**
in your root project properties file , add
```properties
#Telereso
teleresoKmpVersion=<latest-version>
```

**settings.gradle.kts**

Make sure you have this in your root project `settings.gradle.kts` file

```kotlin
pluginManagement {
    val teleresoKmpVersion: String by settings
    plugins {
        id("io.telereso.kmp") version teleresoKmpVersion apply false
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
```

**module/build.gradle.kts** <br>
**lib/build.gradle.kts**

Now you can use the plugin in your module's `build.gradle.kts`

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization")

    id("io.telereso.kmp") // <---- no need to add version
}
```

Make sure to add the generated files into your src directory 

```kotlin
sourceSets {
    val commonMain by getting {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            // your dependencies
        }
    }
}
```

---

## Configuration

The plugin has some configuration you can enable or disable , you can control them in the plugin's
DSL section inside your module's `build.gradle.kts`

```kotlin
teleresoKmp {
    // set to true to stop generated model files with json converters extensions
    disableJsonConverters = true

    // set to true to stop copying generated reactNative files react native dir
    disableReactExport = true

    // set to true to stop copying generated flutter files into flutter dir
    disableFlutterExport = true
}
```

{: .note-title }
> Kmp modules
>
> The plugin is aware of [kmp structure](../starter/#structure) so some configurations might be
> disabled/enabled accordingly so use above flags to override default configs or if you using it in
> your own structure.
