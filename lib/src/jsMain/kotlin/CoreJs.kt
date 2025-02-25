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

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.UnsupportedContentTypeException
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.callContext
import io.ktor.client.engine.js.JsClientEngineConfig
import io.ktor.client.engine.mergeHeaders
import io.ktor.client.fetch.AbortController
import io.ktor.client.fetch.ReadableStream
import io.ktor.client.fetch.ReadableStreamDefaultReader
import io.ktor.client.fetch.RequestInit
import io.ktor.client.fetch.fetch
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.sse.SSECapability
import io.ktor.client.plugins.websocket.WebSocketCapability
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.ResponseAdapterAttributeKey
import io.ktor.client.request.isUpgradeRequest
import io.ktor.client.utils.buildHeaders
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.PlatformUtils
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readText
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writer
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.WebSocketExtension
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.readByteArray
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.MANUAL
import org.w3c.fetch.RequestRedirect
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.files.FileReader
import org.w3c.files.FileReaderSync
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise


/**
 * A JavaScript client engine that uses the fetch API to execute requests.
 *
 * To create the client with this engine, pass it to the `HttpClient` constructor:
 * ```kotlin
 * val client = HttpClient(Js)
 * ```
 *
 * You can learn more about client engines from [Engines](https://ktor.io/docs/http-client-engines.html).
 */
object CoreJs : HttpClientEngineFactory<JsClientEngineConfig> {
    override fun create(block: JsClientEngineConfig.() -> Unit): HttpClientEngine =
        CoreJsClientEngine(JsClientEngineConfig().apply(block))
}


internal class CoreJsClientEngine(
    override val config: JsClientEngineConfig,
) : HttpClientEngineBase("ktor-js") {

    override val supportedCapabilities = setOf(HttpTimeoutCapability, WebSocketCapability, SSECapability)

    init {
        check(config.proxy == null) { "Proxy unsupported in Js engine." }
    }

    @OptIn(InternalAPI::class, InternalCoroutinesApi::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()
        val clientConfig = data.attributes[CLIENT_CONFIG]

        if (data.isUpgradeRequest()) {
            return executeWebSocketRequest(data, callContext)
        }

        val requestTime = GMTDate()
        val rawRequest = data.toRaw(clientConfig, callContext)

        val controller = AbortController()
        rawRequest.signal = controller.signal
        callContext.job.invokeOnCompletion(onCancelling = true) {
            controller.abort()
        }

        val rawResponse = commonFetch(data.url.toString(), rawRequest, config)

        val status = HttpStatusCode(rawResponse.status.toInt(), rawResponse.statusText)
        val headers = rawResponse.headers.mapToKtor(data.method, data.attributes)
        val version = HttpProtocolVersion.HTTP_1_1



        val body = CoroutineScope(callContext).readBody(rawResponse)
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

    // Adding "_capturingHack" to reduce chances of JS IR backend to rename variable,
    // so it can be accessed inside js("") function
    @Suppress("UNUSED_PARAMETER", "UnsafeCastFromDynamic", "UNUSED_VARIABLE", "LocalVariableName")
    private fun createWebSocket(
        urlString_capturingHack: String,
        headers: Headers
    ): WebSocket {
        val protocolHeaderNames = headers.names().filter { headerName ->
            headerName.equals("sec-websocket-protocol", true)
        }
        val protocols = protocolHeaderNames.mapNotNull { headers.getAll(it) }.flatten().toTypedArray()
        return when {
            PlatformUtils.IS_BROWSER -> js("new WebSocket(urlString_capturingHack, protocols)")
            else -> {
                val ws_capturingHack = js("eval('require')('ws')")
                val headers_capturingHack: dynamic = object {}
                headers.forEach { name, values ->
                    headers_capturingHack[name] = values.joinToString(",")
                }
                js("new ws_capturingHack(urlString_capturingHack, protocols, { headers: headers_capturingHack })")
            }
        }
    }

    private suspend fun executeWebSocketRequest(
        request: HttpRequestData,
        callContext: CoroutineContext
    ): HttpResponseData {
        val requestTime = GMTDate()

        val urlString = request.url.toString()
        val socket: WebSocket = createWebSocket(urlString, request.headers)

        val session = JsWebSocketSession(callContext, socket)

        try {
            socket.awaitConnection()
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


}

internal suspend fun CoroutineScope.readBody(
    response: org.w3c.fetch.Response
): ByteReadChannel =
    readBodyBrowser(response)

internal suspend fun CoroutineScope.readBodyBrowser(response: Response): ByteReadChannel {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val stream: ReadableStream<Uint8Array> = response.body ?: response.blob().await().stream() ?: return ByteReadChannel.Empty
    return channelFromStream(stream)
}


internal fun CoroutineScope.channelFromStream(
    stream: ReadableStream<Uint8Array>
): ByteReadChannel = writer {
    val reader: ReadableStreamDefaultReader<Uint8Array> = stream.getReader()
    while (true) {
        try {
            val chunk = reader.readChunk() ?: break
            channel.writeFully(chunk.asByteArray())
            channel.flush()
        } catch (cause: Throwable) {
            reader.cancel(cause)
            throw cause
        }
    }
}.channel

@Suppress("UnsafeCastFromDynamic")
internal fun Uint8Array.asByteArray(): ByteArray {
    return Int8Array(buffer, byteOffset, length).asDynamic()
}

internal suspend fun ReadableStreamDefaultReader<Uint8Array>.readChunk(): Uint8Array? =
    suspendCancellableCoroutine { continuation ->
        read().then {
            val chunk = it.value
            val result = if (it.done) null else chunk
            continuation.resumeWith(Result.success(result))
        }.catch { cause ->
            continuation.resumeWithException(cause)
        }
    }

suspend fun Blob.stream(): ReadableStream<Uint8Array> {
    return if (this.asDynamic().stream != undefined) {
        this.asDynamic().stream() as ReadableStream<Uint8Array>
    } else {
        // Fallback: Convert to ArrayBuffer and create a ReadableStream manually
        val arrayBuffer = this.arrayBuffer()
        js("""
            new ReadableStream({
                start: function(controller) {
                    controller.enqueue(new Uint8Array(arrayBuffer));
                    controller.close();
                }
            })
            """
        ) as ReadableStream<Uint8Array>
    }
}

suspend fun Blob.arrayBuffer(): ArrayBuffer {
    return suspendCancellableCoroutine { continuation ->
        val fileReader = FileReader()

        fileReader.onload = { event ->
            continuation.resume(event.target.asDynamic().result as ArrayBuffer)
        }

        fileReader.onerror = { event ->
            continuation.resumeWithException(RuntimeException("Failed to read Blob"))
        }

        fileReader.readAsArrayBuffer(this)
    }
}

private suspend fun WebSocket.awaitConnection(): WebSocket = suspendCancellableCoroutine { continuation ->
    if (continuation.isCancelled) return@suspendCancellableCoroutine

    val eventListener = { event: Event ->
        when (event.type) {
            "open" -> continuation.resume(this)
            "error" -> {
                continuation.resumeWithException(WebSocketException(event.asString()))
            }
        }
    }

    addEventListener("open", callback = eventListener)
    addEventListener("error", callback = eventListener)

    continuation.invokeOnCancellation {
        removeEventListener("open", callback = eventListener)
        removeEventListener("error", callback = eventListener)

        if (it != null) {
            this@awaitConnection.close()
        }
    }
}

private fun Event.asString(): String = buildString {
    append(JSON.stringify(this@asString, arrayOf("message", "target", "type", "isTrusted")))
}


private fun org.w3c.fetch.Headers.mapToKtor(method: HttpMethod, attributes: Attributes): Headers = buildHeaders {
    this@mapToKtor.asDynamic().forEach { value: String, key: String ->
        append(key, value)
    }

//    dropCompressionHeaders(method, attributes)
    Unit
}

internal suspend fun commonFetch(
    input: String,
    init: RequestInit,
    config: JsClientEngineConfig,
): Response = suspendCancellableCoroutine { continuation ->
    val promise: Promise<Response> = when {
        PlatformUtils.IS_BROWSER -> fetch(input, init)
        else -> {
            val options = js("Object").assign(js("Object").create(null), init, config.nodeOptions)
            fetch(input, options)
        }
    }
    promise.then(
        onFulfilled = {
            continuation.resumeWith(Result.success(it))
        },
        onRejected = {
            continuation.resumeWith(Result.failure(Error("Fail to fetch", it)))
        }
    )
}

internal fun AbortController(): AbortController {
    return js("new AbortController()")
}


@OptIn(InternalAPI::class)
internal suspend fun HttpRequestData.toRaw(
    clientConfig: HttpClientConfig<*>,
    callContext: CoroutineContext
): RequestInit {
    val jsHeaders = js("({})")
    mergeHeaders(this@toRaw.headers, this@toRaw.body) { key, value ->
        jsHeaders[key] = value
    }

    val bodyBytes = getBodyBytes(body, callContext)

    return buildObject {
        method = this@toRaw.method.value
        headers = jsHeaders
        redirect = if (clientConfig.followRedirects) RequestRedirect.FOLLOW else RequestRedirect.MANUAL

        bodyBytes?.let { body = Uint8Array(it.toTypedArray()) }
    }
}

@OptIn(DelicateCoroutinesApi::class)
private suspend fun getBodyBytes(content: OutgoingContent, callContext: CoroutineContext): ByteArray? {
    return when (content) {
        is OutgoingContent.ByteArrayContent -> content.bytes()
        is OutgoingContent.ReadChannelContent -> content.readFrom().readRemaining().readByteArray()
        is OutgoingContent.WriteChannelContent -> {
            GlobalScope.writer(callContext) {
                content.writeTo(channel)
            }.channel.readRemaining().readByteArray()
        }
        is OutgoingContent.ContentWrapper -> getBodyBytes(content.delegate(), callContext)
        is OutgoingContent.NoContent -> null
        is OutgoingContent.ProtocolUpgrade -> throw UnsupportedContentTypeException(content)
    }
}

internal fun <T> buildObject(block: T.() -> Unit): T = (js("{}") as T).apply(block)

internal val CLIENT_CONFIG = AttributeKey<HttpClientConfig<*>>("client-config")

@Suppress("CAST_NEVER_SUCCEEDS")
internal class JsWebSocketSession(
    override val coroutineContext: CoroutineContext,
    private val websocket: WebSocket
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
        get() = throw WebSocketException("Websocket ping-pong is not supported in JS engine.")
        set(_) = throw WebSocketException("Websocket ping-pong is not supported in JS engine.")

    override var timeoutMillis: Long
        get() = throw WebSocketException("Websocket timeout is not supported in JS engine.")
        set(_) = throw WebSocketException("Websocket timeout is not supported in JS engine.")

    override var masking: Boolean
        get() = true
        set(_) = throw WebSocketException("Masking switch is not supported in JS engine.")

    override var maxFrameSize: Long
        get() = Long.MAX_VALUE
        set(_) = throw WebSocketException("Max frame size switch is not supported in Js engine.")

    init {
        websocket.binaryType = BinaryType.ARRAYBUFFER

        websocket.addEventListener(
            "message",
            callback = {
                val event = it.unsafeCast<MessageEvent>()

                val frame: Frame = when (val data = event.data) {
                    is ArrayBuffer -> Frame.Binary(false, Int8Array(data).unsafeCast<ByteArray>())
                    is String -> Frame.Text(data)
                    else -> {
                        val error = IllegalStateException("Unknown frame type: ${event.type}")
                        _closeReason.completeExceptionally(error)
                        throw error
                    }
                }

                _incoming.trySend(frame)
            }
        )

        websocket.addEventListener(
            "error",
            callback = {
                val cause = WebSocketException("$it")
                _closeReason.completeExceptionally(cause)
                _incoming.close(cause)
                _outgoing.cancel()
            }
        )

        websocket.addEventListener(
            "close",
            callback = { event: dynamic ->
                val reason = CloseReason(event.code as Short, event.reason as String)
                _closeReason.complete(reason)
                _incoming.trySend(Frame.Close(reason))
                _incoming.close()
                _outgoing.cancel()
            }
        )

        launch {
            _outgoing.consumeEach {
                when (it.frameType) {
                    FrameType.TEXT -> {
                        val text = it.data

                        websocket.send(text.decodeToString(0, 0 + text.size))
                    }
                    FrameType.BINARY -> {
                        val source = it.data as Int8Array
                        val frameData = source.buffer.slice(
                            source.byteOffset,
                            source.byteOffset + source.byteLength
                        )

                        websocket.send(frameData)
                    }
                    FrameType.CLOSE -> {
                        val data = buildPacket { writeFully(it.data) }
                        val code = data.readShort()
                        val reason = data.readText()
                        _closeReason.complete(CloseReason(code, reason))
                        if (code.isReservedStatusCode()) {
                            websocket.close()
                        } else {
                            websocket.close(code, reason)
                        }
                    }
                    FrameType.PING, FrameType.PONG -> {
                        // ignore
                    }
                }
            }
        }

        coroutineContext[Job]?.invokeOnCompletion { cause ->
            if (cause == null) {
                websocket.close()
            } else {
                // We cannot use INTERNAL_ERROR similarly to other WebSocketSession implementations here
                // as sending it is not supported by browsers.
                websocket.close(CloseReason.Codes.NORMAL.code, "Client failed")
            }
        }
    }

    @OptIn(InternalAPI::class)
    override fun start(negotiatedExtensions: List<WebSocketExtension<*>>) {
        require(negotiatedExtensions.isEmpty()) { "Extensions are not supported." }
    }

    override suspend fun flush() {
    }

    @Deprecated(
        "Use cancel() instead.",
        ReplaceWith("cancel()", "kotlinx.coroutines.cancel"),
        level = DeprecationLevel.ERROR
    )
    override fun terminate() {
        _incoming.cancel()
        _outgoing.cancel()
        _closeReason.cancel("WebSocket terminated")
        websocket.close()
    }

    @OptIn(InternalAPI::class)
    private fun Short.isReservedStatusCode(): Boolean {
        return CloseReason.Codes.byCode(this).let { resolved ->

            resolved == null || resolved == CloseReason.Codes.CLOSED_ABNORMALLY
        }
    }
}

