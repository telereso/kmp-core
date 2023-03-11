---
layout: default
title: Starter
nav_order: 2
---

# Starter

The first step is always the hardest and in a project that is expected to work on 7 platform that can be scary 😬 ! 

But no more with this kmp project starter !

You can visit the starter [website here](https://kmp-starter.telereso.io/)

# Project  
The starter will provide a project that can build libraries ,
<br>
You can read on [multiplatform-libraries here](https://kotlinlang.org/docs/multiplatform.html#multiplatform-libraries)

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

## Models

## ReactNative
## Flutter
_Will be available by end of this year (2023)_
## Android App
## iOS App
## Web App
## Jvm Api


