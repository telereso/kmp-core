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

import io.ktor.client.fetch.RequestInit
import io.ktor.client.fetch.Uint8Array
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.ResponseAdapterAttributeKey
import io.ktor.client.utils.buildHeaders
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.CancellationException
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readText
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.WebSocketExtension
import io.telereso.kmp.core.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import kotlin.js.Promise
import kotlin.coroutines.CoroutineContext

@JsName("wx")
external val wx: dynamic

// Helper function for dynamic JS objects
fun <T> jsObject(builder: T.() -> Unit): T {
    return (js("{}") as T).apply(builder)
}

/**
 * Kotlin/JS bridge for WeChat `wx.request`, similar to `fetch()`.
 */
internal fun wxFetch(url: String, init: RequestInit? = null): Promise<dynamic> {

    return Promise { resolve, reject ->
        val wxOptions = jsObject<dynamic> {
            this.url = url
            this.method = init?.method ?: "GET"

            val headerObject = js("{}")
            val headerKeys =
                (init?.headers as? Any)
                    ?.let { js("Object.keys(it)") as Array<String> }
                    ?: emptyArray()
            for (key in headerKeys) {
                headerObject[key] = init?.headers[key]
            }

            this.header = headerObject

            this.data = when {
                init?.body as? Uint8Array != null -> init.body.buffer
                else -> init?.body
            }

            // Ktor's `RequestInit` does not have credentials directly, handle it via headers
//            this.withCredentials = init?.headers?.get(HttpHeaders.Authorization) != null

            this.success = { res: dynamic ->
                resolve(res)
            }
            this.fail = { err: dynamic ->
                reject(Throwable("wx.request failed: ${JSON.stringify(err)}"))
            }
        }

        wx.request(wxOptions)
    }
}

internal suspend fun wxCommonFetch(
    input: String,
    init: RequestInit
): dynamic = suspendCancellableCoroutine { continuation ->
    val promise: Promise<dynamic> = wxFetch(input, init)
    promise.then<dynamic>(
        onFulfilled = {
            continuation.resumeWith(Result.success(it))
        },
        onRejected = {
            continuation.resumeWith(Result.failure(Error("Fail to fetch", it)))
        }
    )
}

@OptIn(InternalAPI::class)
internal suspend fun executeWxFetch(
    callContext: CoroutineContext,
    requestTime: GMTDate,
    data: HttpRequestData,
    rawRequest: RequestInit
): HttpResponseData {
    val rawResponse = wxCommonFetch(data.url.toString(), rawRequest)

    val status =
        HttpStatusCode(rawResponse.statusCode.toString().toInt(), rawResponse.statusCode.toString())
    val headers = buildHeaders {
        val headerKeys =
            (rawResponse.header as? Any)?.let { js("Object.keys(it)") as Array<String> }
                ?: emptyArray()
        for (key in headerKeys) {
            append(key, rawResponse.header[key] as String)
        }
    }
    val version = HttpProtocolVersion.HTTP_1_1


    val body = ByteReadChannel(
        when {
            rawResponse.data is String -> rawResponse.data as String
            else -> JSON.stringify(rawResponse.data)
        }
    )

    val responseBody: Any = data.attributes.getOrNull(ResponseAdapterAttributeKey)
        ?.adapt(data, status, headers, body, data.body, callContext)
        ?: body

    return HttpResponseData(
        status,
        requestTime,
        headers,
        version,
        responseBody,
        callContext
    )
}

internal suspend fun executeWxWebSocketRequest(
    request: HttpRequestData,
    callContext: CoroutineContext
): HttpResponseData {
    val requestTime = GMTDate()

    val urlString = request.url.toString()
    Log.d("CoreJS",urlString)
    val socket = createWxWebSocket(urlString, request.headers)

    val session = WxWebSocketSession(callContext, socket)

    try {
        socket.await()
    } catch (cause: Throwable) {
        callContext.cancel(CancellationException("Failed to connect to $urlString", cause))
        throw cause
    }

    return HttpResponseData(
        HttpStatusCode.SwitchingProtocols,
        requestTime,
        Headers.Empty,
        HttpProtocolVersion.HTTP_1_1,
        session,
        callContext
    )
}

@Suppress("UNUSED_PARAMETER", "UnsafeCastFromDynamic", "UNUSED_VARIABLE", "LocalVariableName")
private suspend fun createWxWebSocket(
    url: String,
    headers: Headers
): Promise<dynamic> {
    return Promise { resolve, reject ->
        val protocolHeaderNames = headers.names().filter { headerName ->
            headerName.equals("sec-websocket-protocol", true)
        }
        val protocols =
            protocolHeaderNames.mapNotNull { headers.getAll(it) }.flatten().toTypedArray()

        val wxOptions = jsObject<dynamic> {
            this.url = url
            this.protocols = protocols

            this.success = { res: dynamic ->
                resolve(res)
            }
            this.fail = { err: dynamic ->
                reject(Throwable("wx.request failed: ${JSON.stringify(err)}"))
            }
        }
        wx.connectSocket(wxOptions)
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
internal class WxWebSocketSession(
    override val coroutineContext: CoroutineContext,
    private val socket: dynamic // WeChat's wx socket instance
) : DefaultWebSocketSession {
    private val _closeReason: CompletableDeferred<CloseReason> = CompletableDeferred()
    private val _incoming: Channel<Frame> = Channel(Channel.UNLIMITED)
    private val _outgoing: Channel<Frame> = Channel(Channel.UNLIMITED)

    override val incoming: ReceiveChannel<Frame> = _incoming
    override val outgoing: SendChannel<Frame> = _outgoing

    override val extensions: List<WebSocketExtension<*>>
        get() = emptyList()

    override val closeReason: Deferred<CloseReason?> = _closeReason

    override var pingIntervalMillis: Long
        get() = throw WebSocketException("Websocket ping-pong is not supported in WeChat socket.")
        set(_) = throw WebSocketException("Websocket ping-pong is not supported in WeChat socket.")

    override var timeoutMillis: Long
        get() = throw WebSocketException("Websocket timeout is not supported in WeChat socket.")
        set(_) = throw WebSocketException("Websocket timeout is not supported in WeChat socket.")

    override var masking: Boolean
        get() = true
        set(_) = throw WebSocketException("Masking switch is not supported in WeChat socket.")

    override var maxFrameSize: Long
        get() = Long.MAX_VALUE
        set(_) = throw WebSocketException("Max frame size switch is not supported in WeChat socket.")

    init {
        socket.onMessage { res: dynamic ->
            val frame: Frame = when (val data = res.data) {
                is String -> Frame.Text(data as String)
                is ArrayBuffer -> Frame.Binary(false, Int8Array(data as ArrayBuffer).unsafeCast<ByteArray>())
                else -> {
                    val error = IllegalStateException("Unknown frame type: ${'$'}{res.type}")
                    _closeReason.completeExceptionally(error)
                    throw error
                }
            }
            _incoming.trySend(frame)
        }

        socket.onError { res: dynamic ->
            val cause = WebSocketException("${'$'}res")
            _closeReason.completeExceptionally(cause)
            _incoming.close(cause)
            _outgoing.cancel()
        }

        socket.onClose { res: dynamic ->
            val reason = CloseReason(res.code as Short, res.reason as String)
            _closeReason.complete(reason)
            _incoming.trySend(Frame.Close(reason))
            _incoming.close()
            _outgoing.cancel()
        }

        launch {
            _outgoing.consumeEach {
                when (it.frameType) {
                    FrameType.TEXT -> socket.send(it.data.decodeToString())
                    FrameType.BINARY -> socket.send(it.data.unsafeCast<ByteArray>())
                    FrameType.CLOSE -> {
                        val data = buildPacket { writeFully(it.data) }
                        val code = data.readShort()
                        val reason = data.readText()
                        _closeReason.complete(CloseReason(code, reason))
                        socket.close()
                    }

                    FrameType.PING, FrameType.PONG -> { /* No-op */
                    }
                }
            }
        }
    }

    @InternalAPI
    override fun start(negotiatedExtensions: List<WebSocketExtension<*>>) {
        require(negotiatedExtensions.isEmpty()) { "Extensions are not supported." }
    }

    override suspend fun flush() {}

    override fun terminate() {
        _incoming.cancel()
        _outgoing.cancel()
        _closeReason.cancel("WebSocket terminated")
        socket.close()
    }
}