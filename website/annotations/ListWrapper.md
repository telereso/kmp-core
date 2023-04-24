---
layout: default
title: ListWrapper
parent: Annotations
nav_order: 3
---

# ListWrapper

The annotation is meant to solve two limitations faced by JS and iOS platforms

* As of now there are some [limitations](https://dev.to/touchlab/jsexport-guide-for-exposing-kotlin-to-js-20l9){:target="_blank"} while working with JS, one of them is there's no support for `List`,
* For iOS nested generic types like (`Flow<List<User>>`) are not interpolated properly and we end up with `NSArray` with no type and need to cast manually

Therefore this annotation will try to cover these issues,

It will generate code to wrap `List` and `Array` in data classes also converting any function return type of `List` into `Array` so it can be consumed by JS and iOS

---

# Usage

Make sure you've setup the [plugin first](../annotations/#setup)

## Class
There are two type of classes the annotation can recognize  

### Data Class (Models)
You need to add the annotation to your data class if your planning to support `List` return type in JS/iOS

```kotlin
import io.telereso.kmp.annotations.ListWrapper

@Serializable
@JsExport
@ListWrapper
data class User(val id: String, val name: String, val age: Int)
```

It will generate wrapper class for both `List` and `Array` like so 

```kotlin
@Serializable
data class UserList(val list: List<User>)

@Serializable
@JsExport
data class UserArray(val array: Array<User>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UserArray

        if (!array.contentEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
         return array.contentHashCode()
    }
}
```

So now we can support the following return types 

* `Flow<List<User>>` -> `Flow<UserList>`
* `Flow<Array<User>>` -> `Flow<UserArray>`

Meaning we flatten the nested types so the iOS interpolator will be able to work as expected 


### Manager

Usually we add this annotation on the sdk manger of the [kmp structure](../starter/#structure){:target="_blank"} so it will handle our `List` return types

```kotlin
@JsExport
@ListWrapper
class MySdkClientManager private constructor(
    databaseDriverFactory: MySdkClientDatabaseDriverFactory? = null,
    private val builder: Builder,
    config: Config? = null
){
    fun getUsersFlow(): Task<CommonFlow<List<User>>> {
        logDebug("getUsersFlow")
        return Task.execute {
            repository.getUsersFlow()
        }
    }
}
```

{: .note-title }
> Repository
>
> Notice that the repository had a matching function to the function we are targeting (`getUsersFlow`), this is a must
> otherwise the annotation would not work and you will face a compilation error.


Now we can use the above function as so 


<div class="code-block kotlin java java-future swift js">
<div class="tab">
  <button class="tablinks kotlin active" onclick="openTab(event, 'kotlin')">Kotlin</button>
  <button class="tablinks swift" onclick="openTab(event, 'swift')">Swift</button>
  <button class="tablinks js" onclick="openTab(event, 'js')">JS</button>
</div>

<div class="tabcontent kotlin active">

{% highlight kotlin %}

```kotlin
MySdkClientManager.getInstance().getUsersFlow().await().collect {
    logDebug(it.size)
}
```

{% endhighlight kotlin %}

</div>

<div class="tabcontent swift">
{% highlight swift %}

```swift
// Notice we used `getUsersListFlow` instead of `getUsersFlow`
MySdkClientManager.getInstance().getUsersListFlow().await().await { flow, error in
    (flow as! CommonFlow<UserList>).watch { (userList: UserList?, error: ClientException?) in
        if let users = userList?.list {
            print("users size:, \(users.count)!")
        } else {
            print(error?.message ?? "")
        }
    }
}
```

{% endhighlight swift %}

</div>

<div class="tabcontent js">
{% highlight kotlin %}

```javascript
// your sdk will always has a refrence to core classes
const Tasks = require('@example/my-sdk').io.telereso.kmp.core.Tasks; 
const MySdkClient = require('@example/my-sdk').com.example.my-sdk.client; 

const manger = new MySdkClient.MySdkClientManager.Builder().build()

async function getUsers(){
    // Notice we used `getUsersArrayFlow` instead of `getUsersFlow`
    let flow = await CoreClient.Tasks.async(MySdkClient.getUsersArrayFlow(manger, null))
    flow.watch((data, error) => {
        console.log(data.array.length)
    })
}
```

{% endhighlight kotlin %}

</div>

</div>

## Function

Sometimes we don't want the annotation to handle all the functions in a manger, so we can use the annotation on targeted function, 

So with the same example above we would  add the annotation to a function directly 

```kotlin
@JsExport
class MySdkClientManager private constructor(
    databaseDriverFactory: MySdkClientDatabaseDriverFactory? = null,
    private val builder: Builder,
    config: Config? = null
){
    @ListWrapper
    fun getUsersFlow(): Task<CommonFlow<List<User>>> {
        logDebug("getUsersFlow")
        return Task.execute {
            repo.getUsersFlow()
        }
    }
}
```