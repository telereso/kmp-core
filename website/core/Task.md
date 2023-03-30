---
layout: default
title: Task
parent: Core
nav_order: 1
---

## [Task](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/index.html){:target="_blank"}

This class should be used by the sdk manger to expose a functionally and handle multithreading as
well,

Usually we expose the repository methods.

## Why?

As of now due to some limitations in some platform like JS and Java we can not use `suspend`, there
for exposing suspended functions directly form the sdk's client manger will not work

Using a Wrapper class such as [Task](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/index.html){:target="_blank"} will provide a unified approach for multithreading by providing
interfaces like `await()` and [get()](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/get.html){:target="_blank"} and other callbacks

## Create a Task

To create a task we can we just need to invoke [Task.execute](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/-companion/execute.html){:target="_blank"} provide return type and our logic goes
inside the body block

```kotlin
Task.excute<String> {
    val response = repo.makeApiCall<String>()
    repo.saveDB(response)
    return response
}
```

---

## Use a Task

There is two ways to get the result from a task

### Callbacks

In all platforms **(except java)** you can use the following callbacks


<div class="code-block kotlin swift js">
<div class="tab">
  <button class="tablinks kotlin active" onclick="openTab(event, 'kotlin')">Kotlin</button>
  <button class="tablinks swift" onclick="openTab(event, 'swift')">Swift</button>
  <button class="tablinks js" onclick="openTab(event, 'js')">JS</button>
</div>

<div class="tabcontent kotlin active">

{% highlight kotlin %}

```kotlin
ClientManager.getInstance().makeApiCall()
    .onSuccess { res ->
        // this runs in the background
        println(res)
    }.onSuccessUI { res ->
        // this runs in the UI thread
        println(res)
    }.onFailure { err ->
        // this runs in the background
        println(err)
    }.onFailureUI { err ->
        // this runs in the UI thread
        println(err)
    }.onCanceled {
        // the job is canceled 
    }
```

{% endhighlight kotlin %}

</div>

<div class="tabcontent swift">
{% highlight swift %}

```swift
ClientManager.getInstance().makeApiCall()
    .onSuccess  { result in
        guard let list = result else { return }
        print(list)
    }.onSuccessUI  { result in
        guard let list = result else { return }
        print(list)
    }.onFailure { err in
        print("Failed: \(err)")
    }.onFailureUI { error in
        print("Failed: \(err)")
    }.onCanceled{
    }
```

{% endhighlight swift %}

</div>

<div class="tabcontent js">
{% highlight kotlin %}

```javascript
ClientManager.getInstance().makeApiCall()
    .onSuccess((result) => {
        console.log(result)
    })
    .onSuccessUI((result) => {
        console.log(result)
    }).onFailure((e) => {
        console.log(e)
    }).onFailureUI((e) => {
        console.log(e)
    }).onCanceled((e) => {
        console.log(e)
    })
```

{% endhighlight kotlin %}

</div>

</div>

---

### Suspended (blocking)

You can still use the task as suspended to use inside your coroutines, <br>
Or in a blocking approach in languages like java and javascript

<div class="code-block kotlin java java-future swift js">
<div class="tab">
  <button class="tablinks kotlin active" onclick="openTab(event, 'kotlin')">Kotlin</button>
  <button class="tablinks java active" onclick="openTab(event, 'java')">Java</button>
  <button class="tablinks java-future active" onclick="openTab(event, 'java-future')">Java (Future)</button>
  <button class="tablinks swift" onclick="openTab(event, 'swift')">Swift</button>
  <button class="tablinks js" onclick="openTab(event, 'js')">JS</button>
</div>

<div class="tabcontent kotlin active">

{% highlight kotlin %}

```kotlin
ClientManager.getInstance().makeApiCall().await()
```

{% endhighlight kotlin %}

</div>

<div class="tabcontent java">
{% highlight java %}

```java
@Controller
public class Controller {
    @RequestMapping("")
    @ResponseBody
    public String getResult() {
        try {
            // get() is blocking interface that returns the result of excpetoin if failed
            return ClientManager.getInstance().makeApiCall.get();
        } catch (CleintException e) {
            return "Failed! : " + e.message;
        }
    }
}
```

{% endhighlight java %}

</div>

<div class="tabcontent java-future">
{% highlight java %}

```java
@Controller
public class Controller {
    @RequestMapping("")
    @ResponseBody
    public String getResult() {
        try {
            // Tasks.future will return a CompletableFuture than you case it's get() interface 
            return Tasks.future(ClientManager.getInstance().makeApiCall).get();
        } catch (Exception e) {
            return "Failed! : " + e.message;
        }
    }
}
```

{% endhighlight java %}

</div>

<div class="tabcontent swift">
{% highlight swift %}

```swift
ClientManager.getInstance().makeApiCall().await(){result, err in
  // this is complation handler that will return both resulte and error
}
```

{% endhighlight swift %}

</div>

<div class="tabcontent js">
{% highlight kotlin %}

```javascript
// your sdk will always has a refrence to core classes
const Tasks = require('@example/my-lib').io.telereso.kmp.core.Tasks; 

async function getResult(){
    // Tasks.async will return you a promise from a task
    await Tasks.async(ClientManager.getInstance().makeApiCall())
}
```

{% endhighlight kotlin %}

</div>

</div>

---

### [onSuccess](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-success.html){:target="_blank"}



Called when a task is done and successful, you can use the result in background thread

---

### [onSuccessUI](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-success-u-i.html){:target="_blank"}

Called when a task is done and successful, you can use the result in main thread so you can make UI
operations (hide loaders),
Both [onSuccess](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-success.html){:target="_blank"} [onSuccessUI](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-success-u-i.html){:target="_blank"} will be invoked

---

### [onFailure](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-failure.html){:target="_blank"}

if the task failed , the result will be a `ClientExcecptoin` ,
if it was an api call you will get the http status and url that failed in case multiple api calls
were made

---

### [onFailureUI](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-failure-u-i.html){:target="_blank"}

if task failed , the result will be a `ClientExcecptoin` , use this callback to update the UI in
main thread (show error UI)
the exception might hold the backend error message

---

### [onCancel](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/on-cancel.html){:target="_blank"}

If the task is canceled ,
Task can be canceled
using [cancel()](https://telereso.github.io/kmp-core/docs/core/0.0.10/-core/io.telereso.kmp.core/-task/index.html#1617735642%2FFunctions%2F-864720431){:target="_blank"}

---

### [get](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/get.html){:target="_blank"}

It's a blocking call to executing the task, best used in java 

You can use [get](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/get.html){:target="_blank"} or check [getOrNull](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/get-or-null.html){:target="_blank"}

---

### [future](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/[jvm]-tasks/future.html){:target="_blank"}

Convert a [Task](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/index.html){:target="_blank"} into a [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html){:target="_blank"}, only available in `Java`

---

### [async]https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/[js]-tasks/async.html{:target="_blank"}

Convert a [Task](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-task/index.html){:target="_blank"} into [Promise](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise){:target="_blank"}, only available in `JS` 