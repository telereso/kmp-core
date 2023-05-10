---
layout: default
title: CommonFlow
parent: Core
nav_order: 2
---

## [CommonFlow](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-common-flow/index.html){:target="_blank"}

Extend normal flow class to support some limitation on platforms like iOS and JS

---

## Create a Flow


<div class="code-block kotlin swift js">
<div class="tab">
  <button class="tablinks kotlin active" onclick="openTab(event, 'kotlin')">Kotlin</button>
  <button class="tablinks swift" onclick="openTab(event, 'swift')">Swift</button>
  <button class="tablinks js" onclick="openTab(event, 'js')">JS</button>
</div>

<div class="tabcontent kotlin active">

{% highlight kotlin %}

```kotlin
// any flow can be converted into a CommonFlow
listOf("1", "2").asFlow().asCommonFlow()
```

{% endhighlight kotlin %}

</div>

<div class="tabcontent swift">
{% highlight swift %}

```swift
// Make sure to cast the result 
Flows.shared.from(list: ["123", "1234"]) as! CommonFlow<NSString>
```

{% endhighlight swift %}

</div>

<div class="tabcontent js">
{% highlight javascript %}

```javascript
// Make sure to cast the result 
Flows.shared.fromArray(["123", "1234"])
```

{% endhighlight javascript %}

</div>

</div>

---

## Use a CommonFlow

<div class="code-block kotlin java java-future swift js">
<div class="tab">
  <button class="tablinks kotlin active" onclick="openTab(event, 'kotlin')">Kotlin</button>
  <button class="tablinks swift" onclick="openTab(event, 'swift')">Swift</button>
  <button class="tablinks js" onclick="openTab(event, 'js')">JS</button>
</div>

<div class="tabcontent kotlin active">

{% highlight kotlin %}

```kotlin
flow.collcet { data ->

}
```

{% endhighlight kotlin %}

</div>

<div class="tabcontent swift">
{% highlight swift %}

```swift
flow.watch { payload, exception in
    if(exception  != nil){
        print(exception)
    } else {
        print(payload)
    }
}
```

{% endhighlight swift %}

</div>

<div class="tabcontent js">
{% highlight kotlin %}

```javascript
flow.watch((data, error) => {
    if (error != null) {
     console.log(error)
    } else {
     console.log(data);
    }
})

// OR using two call backs 

CoreClient.watch(flow, (data) => {
    console.log(data);
}, (error) => {
    console.log(error)
})

```

{% endhighlight kotlin %}

</div>

</div>

---