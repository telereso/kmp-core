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
import io.telereso.kmp.core.ContextScope
import io.telereso.kmp.core.DispatchersProvider
import io.telereso.kmp.core.Log
import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.Settings
import io.telereso.kmp.core.Utils
import io.telereso.kmp.core.Utils.launchPeriodicAsync
import io.telereso.kmp.core.models.ExpirableValue
import io.telereso.kmp.core.models.fromJson
import io.telereso.kmp.core.models.toJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import kotlin.js.Promise
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

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


@JsExport
fun setupWeChatStorage() {
    Settings.weChatSettings = { duration -> WeChatSetting(duration) }
}

class WeChatSetting(
    clearExpiredKeysDuration: Duration? = null
) : Settings {

    override var listener: Settings.Listener? = null
    private var removeExpiredJob: Deferred<Unit>? = null
    private val flow = MutableStateFlow(mutableMapOf<String, String?>())

    init {
        clearExpiredKeysDuration?.let {
            removeExpiredJob = ContextScope.get(DispatchersProvider.Default)
                .launchPeriodicAsync(it) {
                    removeExpiredKeys()
                }
        }

        flow.value = keys.associateWith { getStringOrNull(it) }.toMutableMap()
    }

    override val keys: Set<String>
        get() = (wx.getStorageInfoSync().keys as Array<String>).toSet()
    override val size: Int
        get() = wx.getStorageInfoSync().currentSize as Int

    override fun clear() {
        wx.clearStorageSync()
        flow.value = mutableMapOf()
    }

    override fun removeExpiredKeys() {
        logDebug("RemoveExpiredKeys - size: $size")
        keys.forEach {
            getExpirableString(it)
        }
        listener?.onRemoveExpiredKeys()
    }

    override fun cancelRemovingExpiredKeys() {
        removeExpiredJob?.cancel()
        removeExpiredJob = null
    }

    override fun remove(key: String) {
        wx.removeStorageSync(key)
        flow.update { it.toMutableMap().apply { remove(key) } }
    }

    override fun hasKey(key: String): Boolean {
        return getStringOrNull(key) != null
    }

    override fun putInt(key: String, value: Int) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getString(key, defaultValue.toString()).toInt()
    }

    override fun getIntOrNull(key: String): Int? {
        return getStringOrNull(key)?.toIntOrNull()
    }

    override fun putLong(key: String, value: Long) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getString(key, defaultValue.toString()).toLong()
    }

    override fun getLongOrNull(key: String): Long? {
        return getStringOrNull(key)?.toLongOrNull()
    }

    override fun putString(key: String, value: String) {
        wx.setStorageSync(key, value)
        flow.update { it.toMutableMap().apply { put(key, value) } }
    }

    override fun getString(key: String, defaultValue: String): String {
        return getStringOrNull(key) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return wx.getStorageSync(key) as String?
    }

    override fun putFloat(key: String, value: Float) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getString(key, defaultValue.toString()).toFloat()
    }

    override fun getFloatOrNull(key: String): Float? {
        return getStringOrNull(key)?.toFloatOrNull()
    }

    override fun putDouble(key: String, value: Double) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getString(key, defaultValue.toString()).toDouble()
    }

    override fun getDoubleOrNull(key: String): Double? {
        return getStringOrNull(key)?.toDoubleOrNull()
    }

    override fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
        flow.update { it.toMutableMap().apply { put(key, value.toString()) } }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key, defaultValue.toString()).toBoolean()
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return getStringOrNull(key)?.toBoolean()
    }

    override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return flow.map { it[key]?.toIntOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getIntFlow(key: String): Flow<Int?> {
        return flow.map { it[key]?.toIntOrNull() }.distinctUntilChanged()
    }

    override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return flow.map { it[key] ?: defaultValue }.distinctUntilChanged()
    }

    override fun getStringFlow(key: String): Flow<String?> {
        return flow.map { it[key] }.distinctUntilChanged()
    }

    override fun getLongFlow(key: String): Flow<Long?> {
        return flow.map { it[key]?.toLongOrNull() }.distinctUntilChanged()
    }

    override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return flow.map { it[key]?.toLongOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return flow.map { it[key]?.toFloatOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getFloatFlow(key: String): Flow<Float?> {
        return flow.map { it[key]?.toFloatOrNull() }.distinctUntilChanged()
    }

    override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> {
        return flow.map { it[key]?.toDoubleOrNull() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getDoubleFlow(key: String): Flow<Double?> {
        return flow.map { it[key]?.toDoubleOrNull() }.distinctUntilChanged()
    }

    override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return flow.map { it[key]?.toBoolean() ?: defaultValue }.distinctUntilChanged()
    }

    override fun getBooleanFlow(key: String): Flow<Boolean?> {
        return flow.map { it[key]?.toBoolean() }.distinctUntilChanged()
    }

    override fun putExpirableString(key: String, value: String, exp: Long) {
        putString(key, ExpirableValue(value, exp).toJson())
    }

    override fun getExpirableString(key: String, default: String?): String? {
        val v = getStringOrNull(key) ?: return default
        val ev = ExpirableValue.fromJson(v)
        if (Utils.isExpired(ev.exp)) {
            logDebug("Removing expired - $key")
            remove(key)
            return default
        }
        return ev.value
    }

    override fun getExpirableString(key: String): String? {
        return getExpirableString(key, null)
    }
}