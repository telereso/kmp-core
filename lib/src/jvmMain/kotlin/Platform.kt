package io.telereso.kmp.core

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class JvmPlatform : Platform {
    override val type: Platform.TYPE = Platform.TYPE.JVM
    override val name: String = type.name.lowercase()
}

/**
 * expected Platform imlmentation for JVM platform
 * @return the platform .
 */
actual fun getPlatform(): Platform = JvmPlatform()

actual fun httpClient(
    shouldLogHttpRequests: Boolean ,
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
        // Here we can add out usual network inteceptors.
        addInterceptor(logging)
        config {
            retryOnConnectionFailure(true)
            connectTimeout(0, TimeUnit.SECONDS)
        }
    }
}