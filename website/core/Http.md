---
layout: default
title: Http
parent: Core
nav_order: 3
---

## [Http](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-http/index.html){:target="_blank"}

This helper class provides functionality to streamline networking operations. 

It includes methods for setting up connections, handling errors, and performing HTTP requests. 

With this class, you can simplify the process of making API calls and improve the reliability of your networking code

---

### ktorConfigJson 

```kotlin
Http.ktorConfigJson
```

Use this configuration to convert `Serializable` classes from/into json while ignoring Unknown Keys.

---

### Http Response Validator

```kotlin
Http.getHttpResponseValidator()
```

A helper function to convert http errors into a `ClientException` object.

you can add this when creating your ktor http client 

```kotlin
val client = HttpClient() {
    install(ContentNegotiation) {
        json(Http.ktorConfigJson)
    }
}.config {
    HttpResponseValidator {
        Http.getHttpResponseValidator(this)
    }
}
```

---

### User Agent

The `UserAgent` helper ensures that all your network requests have a user agent, including those made from mobile devices.

```kotlin
val platform = AndroidPlatform()
Http.getUserAgent(
    platform = platform,
    clientSDKName = BuildKonfig.SDK_NAME,
    clientSDKVersion = BuildKonfig.SDK_VERSION,
    appName = "",  // get from the consumer app
    appVersion = "" // get from the consumer app
)
```

to setup `BuildKonfig` check this 






