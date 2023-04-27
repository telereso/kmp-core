---
layout: default
title: ReactNativeExport
parent: Annotations
nav_order: 4
---

# ReactNativeExport

To export kmp into react native we need this annotation to be added to the [Client Manager](../starter/#clientmanager),

The annotation will wrap the sdk native output (AAR/Framework) and create a [ReactNative Module](https://reactnative.dev/docs/native-modules-intro){:target="_blank"} by generating the following files

* Android - Client Module
* iOS - Swift Module
* iOS - Objective-C Bridge
* JS - Index

---

# Usage

Add the annotation to the Client Manger

```kotlin
@JsExport
@ReactNativeExport
class MySdkClientManager private constructor(
databaseDriverFactory: MySdkClientDatabaseDriverFactory? = null,
private val builder: Builder,
config: Config? = null
) {
    fun getUsers() : List<Users>{
        return Task.execute {
            repo.getUsers()
        }
    }
}
```

This will interpolate the manger functions into android, ios and JS react native module

---

## Android - Client Module

For the above example a file will be generated and updated under the [ReactNative Folder](../starter/#reactnative) of the [kmp structure](../starter/#structure),

It will look something like this 

`{root}/react-native-my-sdk/android/src/main/java/com/example/my_sdk/MySdkModule.g.kt`
```kotlin
@OptIn(ExperimentalJsExport::class)
class MySdkClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val manager = MySdkClientManager.getInstance()

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun getUsers(reason: String, promise: Promise) {
       try {
          promise.resolve(User.toJson(manager.getUsers())) 
       } catch (e:Exception){
          promise.reject(e)
       }

  }

  companion object {
    const val NAME = "MySdkClient"
  }
}
```


{: .note-title }
> .g.kt
>
> Notice the file had `.g.kt` extension which indicate it was generated, 
> it is ignored by default but you can committed anyway to keep track of the changes,<br>
> Also  you cant to eject from the code generation and add your own logic to the module.

The generated code utilise the `@ReactMethod` annotation to expose the logic to react native

---

## iOS - Swift Module

For the above example a file will be generated and updated under the [ReactNative Folder](../starter/#reactnative) of the [kmp structure](../starter/#structure),

It will look something like this

`{root}/react-native-my-sdk/ios/MySdkClient.swift`

```swift
import MySdkClient

extension String: Error {
}

public class MySdkClientInstance {
   public static var shared : MySdkClientManager? = nil
}


@objc(MySdkClient)
class MySdkClient: RCTEventEmitter {

    private var hasListeners = false;

    override func supportedEvents() -> [String]! {
        return []
    }

    override func startObserving() {
        hasListeners = true
    }

    override func stopObserving() {
        hasListeners = false
    }

    override func sendEvent(withName name: String!, body: Any!) {
        if (hasListeners) {
            super.sendEvent(withName: name, body: body)
        }
    }

    @objc(getUsers:withRejecter:)
    func getUsers(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (MySdkClientInstance.shared == nil) {
            reject("getUsers error", "MySdkClientManager was not initialized", "MySdkClientManager was not initialized")
        } else {
            resolve(Users.Companian().fromJson(array: MySdkClientInstance.shared!.getUsers()))
        }
    }
}
```

This is the swift wrapper of the sdk, but we still need to provide the object-c headers

---

## iOS - Objective-C Header

We use `RCT_EXTERN_METHOD` to expose the logic to react native

`{root}/react-native-my-sdk/ios/MySdkClient.m`

```swift
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(MySdkClient, RCTEventEmitter)

RCT_EXTERN_METHOD(supportedEvents)

RCT_EXTERN_METHOD(getUsers:
                 (RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
```

---

## JS - Index

To be able to use the ReactNative Module we need the JS wrapper that will invoke the Android,iOS native modules  

For the above example a file will be generated and updated under the [ReactNative Folder](../starter/#reactnative) of the [kmp structure](../starter/#structure),

It will look something like this

`{root}/react-native-my-sdk/src/index.tsx`
```javascript
import { NativeModules, Platform, NativeEventEmitter, EmitterSubscription } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-mysdk-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const MySdkModels = require('@example/my-sdk').com.example.my_sdk.models;


const MySdkClient = NativeModules.MySdkClient
  ? NativeModules.MySdkClient
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const UsersFromJsonArray = MysdkModels.UsersFromJsonArray;


export function getUsers(): Promise<typeof Array<Users>> {
  return new Promise<typeof Array<Users>>((resolve, reject) => {
    MySdkClient.getUsers()
      .then((data: string) => {
        resolve(Users.Companian.FromJsonArray(Users.Companian,data));
      })
      .catch((e: any) => {
        reject(e);
      });
})      
}
```
---

## Disable ReactNative Export

if you don't want the annotation to update the files while keeping the client manager class annotated, add the following in the module's `build.gradle.kts`

```kotlin
teleresoKmp {
    disableReactExport = true
}
```