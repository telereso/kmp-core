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

import Tasks.async
import io.telereso.kmp.core.CommonFlow
import io.telereso.kmp.core.Config
import io.telereso.kmp.core.ContextScope
import io.telereso.kmp.core.CoreClient
import io.telereso.kmp.core.DispatchersProvider
import io.telereso.kmp.core.Task
import io.telereso.kmp.core.TaskConfig
import io.telereso.kmp.core.await
import io.telereso.kmp.core.isReactNativePlatform
import io.telereso.kmp.core.isWeChatPlatform
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.FileRequest
import io.telereso.kmp.core.models.asClientException
import io.telereso.kmp.core.models.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.promise
import kotlin.coroutines.cancellation.CancellationException

/**
 * Return Singleton CoreClient
 */
@JsExport
fun getCoreClient(): CoreClient {
    return CoreClient.get()
}

@JsExport
fun setupReactNative(): Promise<Int> {
    isReactNativePlatform = true
    return setupReactNativeStorage()
}

@JsExport
fun <T> array(list: List<T>) = list.toTypedArray()

@JsExport
fun <T> list(array: Array<T>) = array.toList()

@JsExport
fun <ResultT> asyncTask(task: Task<ResultT>) = task.async()

@JsExport
fun setupWeChat() {
    isWeChatPlatform = true
    setupWeChatStorage()
}

@JsExport
fun <ResultT> asyncTask(task: Task<ResultT>) = task.async()

/**
 * Another way to consume common flows for JS ,
 * @param stream the flow data
 * @param error in case an issue happen while collecting data this call back will be invoked
 */
@JsExport
fun <T> CommonFlow<T>.collectFlow(
    stream: (T) -> Unit,
    error: (ClientException) -> Unit,
    scope: CoroutineScope = ContextScope.get(DispatchersProvider.Default)
): CommonFlow.Job {
    scope.promise {
        try {
            collect {
                stream(it)
            }
        } catch (exception: Throwable) {
            if (exception !is CancellationException)
                error(exception.asClientException())
        }
    }
    return CommonFlow.Job(scope.coroutineContext[Job]!!)
}

@JsExport
fun <T> Task<CommonFlow<T>>.collectAsyncFlow(
    stream: (T) -> Unit,
    error: (ClientException) -> Unit,
    scope: CoroutineScope = ContextScope.get(DispatchersProvider.Default)
): CommonFlow.Job {
    scope.promise {
        try {
            await().collect {
                stream(it)
            }
        } catch (exception: Throwable) {
            if (exception !is CancellationException)
                error(exception.asClientException())
        }
    }
    return CommonFlow.Job(scope.coroutineContext[Job]!!)
}

@JsExport
fun <T> Task<CommonFlow<T>>.collectAsyncFlow(
    stream: (T) -> Unit,
    error: (ClientException) -> Unit,
    scope: CoroutineScope = ContextScope.get(DispatchersProvider.Default)
): CommonFlow.Job {
    scope.promise {
        try {
            await().collect {
                stream(it)
            }
        } catch (exception: Throwable) {
            if (exception !is CancellationException)
                error(exception.asClientException())
        }
    }
    return CommonFlow.Job(scope.coroutineContext[Job]!!)
}

// file request

@JsExport
fun fileRequest(
    base64: String,
    name: String,
    contentType: String,
    progress: (percentage: Int) -> Unit
): FileRequest {
    return FileRequest(base64, name, contentType, progress)
}

@JsExport
fun fileRequestWithContentType(base64: String, name: String, contentType: String) =
    fileRequest(base64, name, contentType, {})

@JsExport
fun fileRequestWithName(base64: String, name: String) =
    fileRequest(base64, name, name.contentType().toString(), {})

@JsExport
fun fileRequestWithProgress(base64: String, name: String, progress: (percentage: Int) -> Unit) =
    fileRequest(base64, name, name.contentType().toString(), progress)

@JsExport
val TasksExamples = io.telereso.kmp.core.TasksExamples

// Builders

@JsExport
fun configBuilder(appName: String, appVersion: String) = Config.builder(appName, appVersion)

@JsExport
fun taskConfigBuilder() = TaskConfig.builder()

@JsExport
fun taskBuilder() = Task.Builder()

@JsExport
fun taskCompanion() = Task.Companion

@JsExport
fun consumerCompanion() = Consumer.Companion

// converters

@JsExport
fun clientExceptionToJson(clientException: ClientException) = clientException.toJson()

@JsExport
fun clientExceptionToJsonPretty(clientException: ClientException) = clientException.toJsonPretty()

@JsExport
fun fileRequestToJson(fileRequest: FileRequest) = fileRequest.toJson()

@JsExport
fun fileRequestToJsonPretty(fileRequest: FileRequest) = fileRequest.toJsonPretty()