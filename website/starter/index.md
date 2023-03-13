---
layout: default
title: Starter
nav_order: 2
---

# Starter

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

The first step is always the hardest. And in any project that is expected to run on 6 platforms, that can be scary 😬 !

But scary no more with this KMP project starter!

You can visit the starter [here](https://kmp-starter.telereso.io/)

# Project

The starter will provide a project that can build libraries,
<br>
You can read more
on [multiplatform-libraries here](https://kotlinlang.org/docs/multiplatform.html#multiplatform-libraries)

Each project will output 2 main SDKs

* Client
* Models

# Structure

The project uses the Gradle build system and is composed of multiple modules. <br>
The source code is organized into two main directories `lib` and `models`. <br>
Additionally, the rest of all the modules are samples and other platform wrappers (eg: ReactNative)

## Lib

The lib module will contain the client SDK, <br>
which can be used as a wrapper for one or multiple backend services, or as common logic that needs to be shared across different parts of the project. <br>

The module is packaged as follows:

#### Cache

Includes your `Dao` and your `Settings` logic

Responsible for handling all database and long-term persistence caching.

The Dao uses `SqlDelight` by Cashapp.

The Settings uses `multiplatform-settings` by Russhwolf.

#### Remote

Includes your api services and handles the http networking.

The services will be using `ktor` by JetBrains team.

#### Repositories

The Repositories module contains the main logic for your client.

You can add your other repositories here, and ensure that both `dao` and `apiService`
are injected so that you can fetch and save data, and provide reactive flows."

#### ClientManager

The `ClientManager` module serves as the entry point to the SDK and is used by consumers.

Its main role is to initialize the SDK and provide an API to the repositories.

Additionally, the manager handles multithreading by wrapping any suspended calls with a `Task`.

---

## Models

The Models module contains any classes that need to be exposed to all platforms. These classes must be `Serializable` and exported to Js. Additionally, it is recommended that they be implemented as data classes.

example

```kotlin
@Serializable
@JsExport
data class User @JvmOverloads constructor(
    val id: String,
    var name: String? = null,
    var age: String? = null
)
```

{: .note-title }
> Internal Models
>
> If your data class is a request or a response wrapper and is not needed to be exposed to clients
> you can keep it in the `lib` module under a `models` package also add `internal` modifier to it.

---

## ReactNative

The `ReactNative` module provides support for bridging Kotlin Multiplatform code into native libraries for both Android and iOS platforms. You can learn how to enable this feature [here]()

The module contains a folder for React Native, which includes the React Native library, as well as examples for both Android and iOS platforms.

---

## Flutter 🏗️

_Will be available by end of this year (2023)_

---

## Android App

The `Android App` module is a sample Android project that demonstrates how to import the client library and provides usage samples for calling APIs and listening to reactive flows.

---

## iOS App

The `iOS App` module is a sample iOS project that demonstrates how to import the client library and provides usage samples for calling APIs and listening to reactive flows. 

Before running the sample, ensure that you run `./gradlew initAll` in the root project.

---

## Web App

The `Web App` module is a sample React project that demonstrates how to import the client library and provides usage samples for calling APIs and listening to reactive flows. 

Before running the sample, ensure that you run `./gradlew initAll` in the root project.

---

## Jvm Api

The `Jvm Api` module is a sample Java Spring Boot project that demonstrates how to import the `Models` library.

---

# Issues

If you encounter any issues while working with the project, you can report them by creating an issue [in the project repository here](https://github.com/telereso/kmp-core)