---
layout: default
title: Core
nav_order: 3
has_children: true
---

# Core

While building multiple kmp sdks there some reusable logic that can encapsulated and reused in each
new project
This project will provide that

---

## Repo

For any issues or ideas you can check the repo [kmp-core](https://github.com/telereso/kmp-core)

#### Latest version
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/telereso/kmp-core)](https://github.com/telereso/kmp-core/releases)

---

## Setup

In your kotlin-multiplatfom project inside your module `build.gradle.kts` add dependency to your
commonMain dependencies

```kotlin
val coreVersion = "0.0.10"
val commonMain by getting {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/resources/kotlin")
    dependencies {
        api("io.telereso.kmp:core:$coreVersion")
    }
}
```

use `api` to expose some logic to other platform,

if you're support ios too and using cocoapods add the follwoing

```kotlin
cocoapods {
    framework {
        export("io.telereso.kmp:core:$coreVersion")
    }
}
```