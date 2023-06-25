---
layout: default
title: SwiftOverloads
parent: Annotations
nav_order: 5
---

# SwiftOverloads

{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

----

When using defaulted params for model's constructor or a function, <br> 
They are still mandatory for swift (limitation present for kotlin `1.8.*` , might be fixed with later versions [KT-38685](https://youtrack.jetbrains.com/issue/KT-38685/Generate-overloaded-obj-c-functions-for-functions-with-default-parameter-values){:target="_blank"})

This annotation is meant to overcome this limitation.

---

## Usage

Make sure you've setup the [plugin first](../annotations/#setup)

Then use the annotation directly on the class

**Class**
```kotlin
@SwiftOverloads
data class User(val id: String, var name: String? = null, var age: Int? = null)
```


Or add to the constructor

**Class Constructor**
```kotlin
data class User @SwiftOverloads constructor(val id: String, var name: String? = null, var age: Int? = null)
```

And add to the functions with optional params

**Function**
```kotlin
data class User @SwiftOverloads constructor(val id: String, var name: String? = null, var age: Int? = null){

    @SwiftOverloads
    fun getInfo(param1: String, param2: String? = null){
        
    }
}
```

Now you can use in your swift code base as follow 

```swift
// Create objects
User.companion.object(id: "123")
User.companion.object(id: "123", name: "abc")
let user = User.companion.object(id: "123", name: "abc", age: 20)

// Inkove Functions 
user.getInfo(param1: "foo")    // param2 will be defaulted to nil
user.getInfo(param1: "foo", param2: "bar")
```

## Configurations

You can add some configuration to the annotation for the following use cases 

### Rename create object method
In case `*.companion.object()` does not match your project naming convention you can change it with `teleresoKmp` gradle extension

at your module's `build.gradle.kts`
```kotlin
teleresoKmp {
    createObjectFunctionName = "instance"
}
```

Now you can create object like so `User.companion.instance(id: "123")`

### Reuse @JvmOverloads

If you have an old project and already using `@JvmOverloads` on all the models and functions , 
No need to revisit all the code base and add `@SwiftOverloads`

You can utilise `@JvmOverloads` to generate the needed overloads for swift too

at your module's `build.gradle.kts`
```kotlin
teleresoKmp {
    swiftOverloadsByJvmOverloads = true // false by default
}
```

Now you will have same end result as using `@SwiftOverloads`