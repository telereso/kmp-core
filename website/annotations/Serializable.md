---
layout: default
title: Serializable
parent: Annotations
nav_order: 1
---

# Serializable

This is an [official annotation](https://kotlinlang.org/docs/serialization.html) in kotlin , but
when adding the [kmp plugin](https://plugins.gradle.org/plugin/io.telereso.kmp){:target="_blank"} it will add extended functionality to it, mainly json converters

## Json converters

To avoid creating `toJson` and `fromJson` for each serializable data class, the plugin will create
extended functions for the annotated data class that do just that !

* `toJson()` Converts from object into string json object
* `toJson(array)` Converts from array into string json array
* `toJsonPretty()` Converts from object into formatted string json object
* `fromJson()` Converts string json object into data class object
* `fromJsonArray()` Converts string json array into array of data class object

The plugin is aware
of [@JsExport](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/-js-export/){:target="_blank"} annotation and
it will create the js function name accordingly

## Usage

Make sure you've setup the [plugin first](../annotations/#setup)

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

## Disable Json converters

### Module Level

if you don't want the json converters to be added to the entire module, add the following in the module's `build.gradle.kts`

```kotlin
teleresoKmp {
    disableJsonConverters = true
}
```

### Class Level

If you want to disable for just one class 

```kotlin
import io.telereso.kmp.annotations.SkipJsonConverters

@Serializable
@JsExport
@SkipJsonConverters
data class User(val id: String, val name: String, val age: Int)
```