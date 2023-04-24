---
layout: default
title: Settings
parent: Core
nav_order: 3
---

# [Settings](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-settings/index.html){:target="_blank"}

To store key/value settings and configurations,

This settings helper is a wrapper
of [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings) by Russhwolf.

The reason a wrapper is used just in case we needed to change the settings library we can do that
with no changes on consuming sdks.

## Usage 

There are two type of settings: 

### Long Term 


Settings will be saved in file and will be available the next session

```kotlin
val settings = Settings.get()
```

### InMemory 


Can be used to store key value settings for the current session only , useful for backend

```kotlin
val settings = Settings.getInMemory
```

## API

It has all same api interfaces

* `getInt` and `getIntOrNull`
* `getLong` and `getLongOrNull`
* `getBoolean` and `getBooleanOrNull`
* `getFloat` and `getFloatOrNull`
* `getDouble` and `getDoubleOrNull`
* `getString` and `getStringOrNull`

### Expirable Settings

One extra useful tool to use is expirable strings

`putExpirableString` and `getExpirableString`

you can set a settings that has an expatriation date

```kotlin
settings.putExpirableString(key = "key_reward", value = "10 points", exp = 1678530427)

settings.getExpirableString(key = "key_reward", default = "0 points")
```