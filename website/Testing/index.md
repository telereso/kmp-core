---
layout: default
title: Core Test
nav_order: 4
has_children: true
---

# Core Test

Helper classes while doing unit, ui and screen shot testing

---

## Unit Testing

Add `core-test` to your `commonTest` section

```kotlin
val coreVersion = "<latest-version>"
val commonTest by getting {
    dependencies {
        implementation("io.telereso.kmp:core-test:$coreVersion")
    }
}
```

## Screenshot Testing

Screenshot testing depend on desktop platform to render the composable functions and store the results

Very compatible with CI/CD because It does not depend on emulators or simulators

