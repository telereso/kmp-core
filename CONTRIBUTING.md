# Structure

Core will have and encapsulate all basic reusable utils that we need to build kmp projects, 
This project is a kmp project as well 


## lib
The main module , you can check `commonMain`
Some of the important classes are 
* [Task](https://github.com/telereso/kmp-core/blob/main/lib/src/commonMain/kotlin/io.telereso.kmp.core/Task.kt)
* [Settings](https://github.com/telereso/kmp-core/blob/main/lib/src/commonMain/kotlin/io.telereso.kmp.core/Settings.kt)
* [ClientException](https://github.com/telereso/kmp-core/blob/main/lib/src/commonMain/kotlin/io.telereso.kmp.core/models/ClientException.kt)
* [Log](https://github.com/telereso/kmp-core/blob/main/lib/src/commonMain/kotlin/io.telereso.kmp.core/Log.kt)

For networking we use ktor with OkHttp engine ,

We use other open source library , but we wrap them in case we need to change later to other libraries or something we build

* [SQLDelight](https://github.com/cashapp/sqldelight)
* [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings)
* [Napier](https://github.com/AAkira/Napier)

## Serialization
When adding any new model that needs to be serializable, make sure to add to to package `io.telereso.kmp.core.models` 
This package is expected to be skipped by obfuscation tools 

## Developing Environment
Ideally using a mac will make things simpler because you can test on all environments , 
But as core you can work on any other OS and the pipeline should run and build all environments 

# Versioning
Handled by the ci/cd (using tags)

# Samples
There are 3 projects `androidApp`, `iosApp` and `webApp` you can build any new feature there 

# Project Secrets
Handled by the pipeline , but project can run locally without any secrets 

# Unit Testing 
Most of the important logic is unit tested , 
Currently we are at [![coverage report](https://telereso.github.io/kmp-core/tests/kover/badge.svg)](https://telereso.github.io/kmp-core/tests/kover) but we are working on reaching 90%
To see what is missing to reach that target click on badge above
