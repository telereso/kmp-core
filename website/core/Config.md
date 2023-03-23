---
layout: default
title: Config
parent: Core
nav_order: 4
---

## Config

When using a builder for your SDK manager, there are some reusable fields and attributes that are common between all SDKs. The [Config](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-config/index.html) helper class encapsulates these common attributes and can be added as part of your SDK manager builder

There are two ways to create [Config](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-config/index.html) objects:

---

### Builder

Using the standard constructor-based approach, which is supported on all platforms.

```kotlin
Config.Builder(appName = "test", appVersion = "1.0.0")
    .shouldLogHttpRequests(true)
    .withEnvironment(Environment.STAGING)
    .withHost("http", "example.com")
    .withConnectTimeoutInMillis(2000)
    .withRequestTimeoutInMillis(3000)
    .withInterceptors(listOf()) // set okHttp Interceptors
    .build()
```

---

### DSL

Using the DSL (domain-specific language) builder, which is available for Android.

```kotlin
Config.builder(appName = "test", appVersion = "1.0.0") {
    shouldLogHttpRequests(true)
    withEnvironment(Environment.STAGING)
    withHost("http", "example.com")
    withConnectTimeoutInMillis(2000)
    withRequestTimeoutInMillis(3000)
    withInterceptors(listOf()) // set okHttp Interceptors  
}
```