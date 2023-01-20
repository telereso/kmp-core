package io.telereso.core.client

import android.os.Build
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * A AndroidPlatform class that defines the Android platform type and name.
 * Referernces the names from the Android platform specific [Build] class
 */
class AndroidPlatform : Platform {
    override val type: Platform.TYPE = Platform.TYPE.ANDROID
    override val name: String = "sdk/${Build.VERSION.RELEASE} device/${Build.MANUFACTURER}-${Build.MODEL}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun httpClient(
    shouldLogHttpRequests: Boolean,
    interceptors: List<Any?>?,
    userAgent: String?,
    config: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp) {

    // https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor
    val logging = HttpLoggingInterceptor().also {
        it.setLevel(HttpLoggingInterceptor.Level.BODY)
    }
    config(this)

    install(UserAgent) {
        agent = userAgent?: getPlatform().name
    }
    /**
     * needed to define the transformation of the HttClient.
     * JsonFeature was replaced by ContentNegotiation in Ktor 2 above
     * https://ktor.io/docs/serialization-client.html#add_content_negotiation_dependency
     * Ktor has a set of plugins already built in that are disabled by default. The
    ContentNegotiation, for example, allows you to deserialize responses,
     */
    /**
     * needed to define the transformation of the HttClient.
     * JsonFeature was replaced by ContentNegotiation in Ktor 2 above
     * https://ktor.io/docs/serialization-client.html#add_content_negotiation_dependency
     * Ktor has a set of plugins already built in that are disabled by default. The
    ContentNegotiation, for example, allows you to deserialize responses,
     */
    install(ContentNegotiation) {
        /**
         * Since it’s JSON, you can install
        ContentNegotiation for json so the response from this function will be the
        deserialized object.
        Also contentnegotion is needed to pass data objects as body in a request.
         */
        /**
         * Since it’s JSON, you can install
        ContentNegotiation for json so the response from this function will be the
        deserialized object.
        Also contentnegotion is needed to pass data objects as body in a request.
         */
        json(Http.ktorConfigJson)
    }

    engine {
        interceptors?.forEach {
            if (it is Interceptor)
                addInterceptor(it)
        }
        // Here we can add out usual network inteceptors.
        if(shouldLogHttpRequests) {
            // Here we can add out usual network inteceptors.
            addInterceptor(logging)
        }
        //addInterceptor(ChuckerInterceptor(context))
        config {
            retryOnConnectionFailure(true)
            connectTimeout(0, TimeUnit.SECONDS)
        }
    }
}

/**
 * Called from the client to initialize Napier logger
 */
fun initLogger() {
    Napier.base(DebugAntilog())
}