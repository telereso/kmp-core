---
layout: default
title: Annotations
nav_order: 4
has_children: true
---

# Annotations

Telereso plugin offers some useful annotations to support the [kmp structure](html){:target="_blank"}starter/#structure)
,

Some of this annotations can be used in other project structures too like [@builder]()

---

# Repo

For any issues or ideas you can check the repo [kmp-annotations](https://github.com/telereso/kmp-annotations)

---

# Setup

To be able to use Telereso annotations you need to add
the [kmp plugin](https://plugins.gradle.org/plugin/io.telereso.kmp)

**settings.gradle.kts**

Make sure you have this in your root project `settings.gradle.kts` file

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
```

**build.gradle.kts**

Add the kmp plugin in the plugin section in your root project `build.gradle.kts` file

```kotlin
plugins {
//trick: for the same plugin versions in all sub-modules

    id("org.jetbrains.kotlin.android").version("1.7.21").apply(false)
    id("org.jetbrains.kotlin.plugin.parcelize").version("1.7.20").apply(false)
    kotlin("multiplatform").version("1.7.21").apply(false)
    id("org.jetbrains.kotlin.native.cocoapods").version("1.7.22").apply(false)
    id("com.squareup.sqldelight").version("1.5.3").apply(false)

    id("io.telereso.kmp").version("0.0.14").apply(false) // <------- add the latest version
}
```

**module/build.gradle.kts**

Now you can use the plugin in your module's `build.gradle.kts`

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization")

    id("io.telereso.kmp") // <---- no need to add version
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
> disabled/enabled accordingly so use above flags to override default configs or if you using in
> your own structure.
