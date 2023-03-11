---
layout: default
title: Http
parent: Core
nav_order: 2
---

## Http

Helper Class to setup some networking operations 

### ktorConfigJson 

```kotlin
Http.ktorConfigJson
```

Use this configuration to convert `Serializable` classes from/into json while ignoring Unknown Keys

### Http Response Validator

```kotlin
Http.getHttpResponseValidator()
```

Helper function to convert http errors into `ClientException`

you can added this when creating your ktor http client 

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

### User Agent

To make sure all your network requests have a user agent (even mobile)

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






