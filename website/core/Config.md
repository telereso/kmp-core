---
layout: default
title: Config
parent: Core
nav_order: 4
---

## Config

When using a builder for your sdk manger there are some reusable fields and attributes
that are common between all sdks , this helper will encapsulate them

You can add this class as part of your sdk manager builder

There are two type to create the config objects

### Builder

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

### DSL

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