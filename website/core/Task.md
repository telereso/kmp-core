---
layout: default
title: Task
parent: Core
nav_order: 1
---

## Task

This class should be used by the sdk manger to expose a functionally and handle multithreading as well,

Usually we expose the repository methods

Create a Task

```kotlin
Task.excute<String> {
    val response = repo.makeApiCall<String>()
    repo.saveDB(response)
    return response
}
```

Use a Task

```kotlin
ClientManger.makeApiCall()
    .onSuccess {
        // this runs in the background
    }.onSuccessUI {
        // this runs in the UI thread
    }.onFailed {
        // this runs in the background
    }.onFailedUI {
        // this runs in the UI thread
    }.onCanceled {
        // the job is canceled 
    }
```

### onSuccess
Called when a task is done and successful, you can use the result in background thread 
### onSuccessUI
Called when a task is done and successful, you can use the result in main thread so you can make UI operations (hide loaders), 
Both `onSuccess` `onSuccessUI` will be invoked
### onFailed
if the task failed , the result will be a `ClientExcecptoin` , 
if it was an api call you will get the http status and url that failed in case multiple api calls were made
### onFailedUI
if task failed , the result will be a `ClientExcecptoin` , use this callback to update the UI in main thread (show error UI)
the exception might hold the backend error message 
### onCanceled
If the task is canceled , 
Task can be canceled using [cancel()](https://telereso.github.io/kmp-core/docs/core/0.0.10/-core/io.telereso.kmp.core/-task/index.html#1617735642%2FFunctions%2F-864720431)

