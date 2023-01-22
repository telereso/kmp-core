package io.telereso.kmp.core

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConfigTest {

    @Test
    fun shouldSetConfigAsExpected() {
        Config.builder("appVersion1.0.0") {
            shouldLogHttpRequests(true)
            withEnvironment(Environment.STAGING)
            withHost("http", "example.com")
            withConnectTimeoutInMillis(2000)
            withRequestTimeoutInMillis(3000)
            withInterceptors(listOf("ChuckerPlease"))
        }.builder.run {
            logHttpRequests.shouldBeTrue()
            environment.shouldBe(Environment.STAGING)
            host.shouldBe("example.com")
            protocol.shouldBe("http")
            connectTimeoutMillis.shouldBe(2000L)
            requestTimeoutMillis.shouldBe(3000L)
            interceptors.shouldNotBeEmpty()
            interceptors?.first().shouldBe("ChuckerPlease")
            appVersion.shouldBe("appVersion1.0.0")
        }
    }
}