---
layout: default
title: Builder
parent: Annotations
nav_order: 2
---

# Builder

While having a builder alongside Kotlin constructors might seem redundant, it can be useful when using models in Java and especially in backend development. Therefore, the `Builder` annotation was added.

---

## Usage

In your module `build.gradle.kts` extend your source set dirs by adding the following line
inside `commonMain`

```kotlin
val commonMain by getting {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/resources/kotlin") // <----- add this line
    dependencies {}
}
```

Then use the annotation on your data class and add companion object as the following

```kotlin
@Serializable
@JsExport
@Builder
data class User(val id: String, var name: String? = null, var age: Int? = null) {
    /**
     * Converts the object into a json object
     */
    fun json(): String {
        return toJson()
    }

    /**
     * Converts the object into a formatted json object
     */
    fun jsonPretty(): String {
        return toJsonPretty()
    }

    companion object {
        @JvmStatic
        fun builder(id: String): UserBuilder {
            return UserBuilder(id)
        }
    }
}
```

{: .note-title }
> Nullability
>
> For the builder annotation to be useful make sure to have most of the fields as nullable,
> otherwise you gonna have to manually add the the none nullable fields to the companion builder
> function just like `id` in the example above.

Now you can use the builder


```java
public class Test {
    public static void main(String[] args) {
        User user = User.builder("1")
                .name("foo")
                .age(20)
                .build();

        user.json();
    }
}
```
