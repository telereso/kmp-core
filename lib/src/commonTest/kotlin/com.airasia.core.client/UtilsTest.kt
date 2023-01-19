package com.airasia.core.client

import com.airasia.core.client.Log.logError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlin.test.Test

class UtilsTest {

    private val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiQUNDRVNTX1RPS0VOIiwidXNlcklkIjoiNTU1MDA5NGQtYzUzZi00YzhhLWE0NDYtZjdjNzM1MmQyNTlkIiwic2Vzc2lvbklkIjoiWXg2SElHS1ZQWUZ4UEFZSlBLTHAiLCJpYXQiOjE2NzEwODU1MDQsImV4cCI6MTY3MTA4NjEwNH0.fGALCFFf47kgcXdcTFIPYGY1e9PbNgI96GXN9hjC5zo"
    private val accessTokenFailed = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ10eXBlIjoiQUNDRVNTX1RPS0VOIiwidXNlcklkIjoiNTU1MDA5NGQtYzUzZi00YzhhLWE0NDYtZjdjNzM1MmQyNTlkIiwic2Vzc2lvbklkIjoiWXg2SElHS1ZQWUZ4UEFZSlBLTHAiLCJpYXQiOjE2NzEwODU1MDQsImV4cCI6MTY3MTA4NjEwNH0.fGALCFFf47kgcXdcTFIPYGY1e9PbNgI96GXN9hjC5zo"

    @Test
    fun hasExpiredShouldBeTrueGivenIllegalToken() {
        shouldThrow<ClientException> {
            Utils.hasJwtExpired("",300)
        }

        shouldThrow<ClientException> {
            Utils.hasJwtExpired("hello",0)
        }

        shouldThrow<ClientException> {
            Utils.hasJwtExpired(accessTokenFailed,0).shouldBe(true)
        }

        ClientException.listener = {
           logError(it)
        }

    }

    @Test
    fun hasExpiredShouldBeTrueGivenRealExpiredAccessToken() {
        Utils.unitTestInstance = Instant.DISTANT_FUTURE
        Utils.hasJwtExpired(accessToken,300).shouldBe(true)
    }

    @Test
    fun hasExpiredShouldBeFalseGivenRealNotExpiredAccessToken() {
        Utils.unitTestInstance = Instant.DISTANT_PAST
        Utils.hasJwtExpired(accessToken,300).shouldBe(false)
    }

    @Test
    fun hasExpiredShouldBeTrueGivenExpiredAccessToken() {
        // has expired
        // where 1671086104 is the accessTokne in EpochSeconds and 400 is the now time we substract
        Utils.unitTestInstance = Instant.fromEpochSeconds(1671086104 - 400)
        Utils.hasJwtExpired(accessToken,300).shouldBe(false)

        // has not expired
        Utils.unitTestInstance = Instant.fromEpochSeconds(1671086104 - 200)
        Utils.hasJwtExpired(accessToken,300).shouldBe(true)
    }
}