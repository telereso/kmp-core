---
layout: default
title: Serializable
parent: Annotations
nav_order: 1
---

# Serializable

This is an [official annotation](https://kotlinlang.org/docs/serialization.html) in kotlin , but
when adding the [kmp plugin] it will add extended functionality to it, mainly json converters

## Json converters

To avoid creating `toJson` and `fromJson` for each serializable data class, the plugin will create
extended functions for the annotated data class that do just that !

* `toJson()` Converts from object into string json object
* `toJson(array)` Converts from array into string json array
* `toJsonPretty()` Converts from object into formatted string json object
* `fromJson()` Converts string json object into data class object
* `fromJsonArray()` Converts string json array into array of data class object

The plugin is aware
of [@JsExport](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/-js-export/) annotation and
it will create the js function name accordingly

## Usage

in your module `build.gradle.kts` extend your source set dirs by adding the following line inside `commonMain`

```kotlin
val commonMain by getting {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/resources/kotlin") // <----- add this line
    dependencies {}
}
```

Then use the annotation on your data class

```kotlin
@Serializable
@JsExport
data class User(val id: String, val name: String, val age: Int)
```

Now you can use the extension functions to convert to json 

```kotlin
val user = User("1", "foo", 20)
val users = listOf(user,user)

println(user.toJson())
println(user.toJsonPretty())
println(User.toJson(users.toTypedArray()))
```

_output_

```json
{"id":"1","name":"foo","age":20}
```
```json
{
  "id": "1",
  "name": "foo",
  "age": 20
}
```
```json
[{"id":"1","name":"foo","age":20},{"id":"1","name":"foo","age":20}]
```

And create objects from json


```kotlin
val user = User.fromJson("{\"id\":\"1\",\"name\":\"foo\",\"age\":20}")

val users = User.fromJsonArray("[{\"id\":\"1\",\"name\":\"foo\",\"age\":20},{\"id\":\"1\",\"name\":\"foo\",\"age\":20}]")
```

