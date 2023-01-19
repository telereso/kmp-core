package com.airasia.core.client.rn

import com.airasia.core.client.DatabaseDriverFactory
import CoreClientManager
import com.facebook.react.bridge.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.js.ExperimentalJsExport


@OptIn(ExperimentalJsExport::class)
class CoreClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val sdk = CoreClientManager.Builder(DatabaseDriverFactory(reactApplicationContext)).build()

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun fetchLaunchRockets(force: Boolean, promise: Promise) {
    sdk.fetchLaunchRockets(force).onSuccess {
      promise.resolve(RocketLaunch.toJson(it))
    }.onFailure {
      promise.reject(it)
    }

  }

  companion object {
    const val NAME = "CoreClient"
  }
}
