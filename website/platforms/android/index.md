---
layout: default
title: Android
parent: Platforms
nav_order: 1
---

# Android

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

Using a sdk generated from a [kmp structure](../starter/#structure) in android project is pretty simple and similar to adding any other sdk

## Setup

just add the android version of the sdk in your `build.gradle`

```groovy
dependencies {
    implementation 'com.example.mysdk-client:mysdk-client-android:1.0.0'
}
```

the models will exposed from the client it self , in case you needed the models only, you can do the follwoing 

```groovy
dependencies {
    implementation 'com.example.mysdk-models:mysdk-models-android:1.0.0'
}
```


## Pro-guard

Make sure to keep your sdks models so the serialization can work in your release flavors

inside `proguard-rules.txt` add the follwoing 

```text
-keep class io.telereso.kmp.core.models.** {*;}      # core models
-keep class com.example.mysdk.models.** {*;}         # your models
-keep class com.example.mysdk.client.models.** {*;}  # clients models
```


