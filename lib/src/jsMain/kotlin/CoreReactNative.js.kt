/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


private val AsyncStorage: dynamic = try {
   js("require('@react-native-async-storage/async-storage')").default
} catch (e: Throwable) {
    console.warn("⚠️ AsyncStorage not found. Falling back to in-memory storage.")
    null
}

// In-memory fallback storage
private val syncStorage = mutableMapOf<String, String>()

@JsExport
fun setupReactNativeStorage(platform: String? = null) {
    if (platform == "web") return

    val localStorage: dynamic = js("{}")

// Assign functions dynamically
    localStorage.setItem = { key: String, value: String ->
        syncStorage[key] = value
        AsyncStorage?.setItem(key, value)
    }

    localStorage.getItem = { key: String ->
        syncStorage[key]
    }

    localStorage.removeItem = { key: String ->
        syncStorage.remove(key)
        AsyncStorage?.removeItem(key)
    }

    localStorage.clear = {
        syncStorage.clear()
        AsyncStorage?.clear()
    }

    localStorage.length = {
        syncStorage.size
    }

    localStorage.key = { index: Int ->
        syncStorage.keys.elementAtOrNull(index)
    }

    js("global.localStorage = localStorage")

    if (AsyncStorage != null) {
        AsyncStorage.getAllKeys()
            ?.then { keys: Array<String> ->
                keys.forEach { key ->
                    AsyncStorage.getItem(key).then { value ->
                        if (value != null) syncStorage[key] = value
                    }
                }
            }
            ?.catch { console.warn("⚠️ AsyncStorage is unavailable.") }
    }

}