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

The first step is always the hardest and in a project that is expected to work on 7 platform that
can be scary 😬 !

But no more with this kmp project starter !

You can visit the starter [website here](https://kmp-starter.telereso.io/)

# Project

The starter will provide a project that can build libraries ,
<br>
You can read
on [multiplatform-libraries here](https://kotlinlang.org/docs/multiplatform.html#multiplatform-libraries)

Each project will output 2 main sdks

* Client
* Models

# Structure

The project is a gradle multi-module , <br>
The source code will be split between `lib` and `models` <br>
The rest of the modules are samples and other platform wrappers (eg: ReactNative)

## Lib

It will be your client sdk , <br>
A client can be a one backend service wrapper or multiple services <br>
Or it can be just common logic that needs to be shared,

The lib module is package as following

### Cache

Includes your `Dao` and your `Settings` logic

Handle all database and long persistence caching

The Dao will be using `SqlDelight`

### Remote

Includes your api services and to handle the http networking

The services will be using `ktor`

### Repositories

The main logic of your client will be here

Add your main other repositories, and make sure to inject both `dao` and `apiService`
so you can fetch and save data and provide flows

### ClientManager

Entry point to the sdk and used by consumers

It's job is to init the sdk and provide an API to the repositories

Also the manger will handle the multithreading by wrapper any suspended calls with a `Task`

---

## Models

Any Class that needs to be exposed to all platforms, it has to be `Serializable` and JS exported
data class

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
> you can keep it in `lib` module under a `models` package also add `internal` modifier to it.

---

## ReactNative

Kmp support reactNative by bridging into the native (Android/iOS) libraries , you can check how to
enable [this here]()

your project will have a folder for react native , inside it there is the reactNative library and an
example for both android and iOS

---

## Flutter 🏗️

_Will be available by end of this year (2023)_

---

## Android App

A sample android project that imports the client library with some usage sample for calling apis and
listening to flows

---

## iOS App

A sample iOS project that imports the client library with some usage sample for calling apis and
listening to flows

make sure to run `./gradlw initAll` in root project before using running the sample

---

## Web App

A sample react project that imports the client library with some usage sample for calling apis and
listening to flows

make sure to run `./gradlw initAll` in root project before using running the sample

---

## Jvm Api

A sample java sprint boot project that imports the `models` library

---

# Issues

You can report any issues you face after creating the project by creating an
issue [in the project](https://github.com/telereso/kmp-core)