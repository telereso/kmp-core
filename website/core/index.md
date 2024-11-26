---
layout: default
title: Core
nav_order: 3
has_children: true
---

# Core

While building multiple KMP SDKs, there some reusable logic that can be encapsulated and reused in each
new project
This project aims to provide that.

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
val coreVersion = "<latest-version>"
val commonMain by getting {
    dependencies {
        api("io.telereso.kmp:core:$coreVersion")
    }
}
```

use `api` to expose some logic to other platform,

if you're supporting ios too and using cocoapods add the following

```kotlin
cocoapods {
    framework {
        export("io.telereso.kmp:core:$coreVersion")
    }
}
```