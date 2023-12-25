///*
// * MIT License
// *
// * Copyright (c) 2023 Telereso
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package io.telereso.kmp.core
//
//import io.telereso.kmp.core.Log.logError
//import io.kotest.assertions.throwables.shouldThrow
//import io.kotest.matchers.shouldBe
//import io.telereso.kmp.core.models.ClientException
//import kotlinx.datetime.Instant
//import kotlin.test.Test
//
//class UtilsTest {
//
//    private val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiQUNDRVNTX1RPS0VOIiwidXNlcklkIjoiNTU1MDA5NGQtYzUzZi00YzhhLWE0NDYtZjdjNzM1MmQyNTlkIiwic2Vzc2lvbklkIjoiWXg2SElHS1ZQWUZ4UEFZSlBLTHAiLCJpYXQiOjE2NzEwODU1MDQsImV4cCI6MTY3MTA4NjEwNH0.fGALCFFf47kgcXdcTFIPYGY1e9PbNgI96GXN9hjC5zo"
//    private val accessTokenFailed = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ10eXBlIjoiQUNDRVNTX1RPS0VOIiwidXNlcklkIjoiNTU1MDA5NGQtYzUzZi00YzhhLWE0NDYtZjdjNzM1MmQyNTlkIiwic2Vzc2lvbklkIjoiWXg2SElHS1ZQWUZ4UEFZSlBLTHAiLCJpYXQiOjE2NzEwODU1MDQsImV4cCI6MTY3MTA4NjEwNH0.fGALCFFf47kgcXdcTFIPYGY1e9PbNgI96GXN9hjC5zo"
//
//    @Test
//    fun hasExpiredShouldBeTrueGivenIllegalToken() {
//        shouldThrow<ClientException> {
//            Utils.hasJwtExpired("",300)
//        }
//
//        shouldThrow<ClientException> {
//            Utils.hasJwtExpired("hello",0)
//        }
//
//        shouldThrow<ClientException> {
//            Utils.hasJwtExpired(accessTokenFailed,0).shouldBe(true)
//        }
//
//        ClientException.listener = {
//           logError(it)
//        }
//
//    }
//
//    @Test
//    fun hasExpiredShouldBeTrueGivenRealExpiredAccessToken() {
//        Utils.unitTestInstance = Instant.DISTANT_FUTURE
//        Utils.hasJwtExpired(accessToken,300).shouldBe(true)
//    }
//
//    @Test
//    fun hasExpiredShouldBeFalseGivenRealNotExpiredAccessToken() {
//        Utils.unitTestInstance = Instant.DISTANT_PAST
//        Utils.hasJwtExpired(accessToken,300).shouldBe(false)
//    }
//
//    @Test
//    fun hasExpiredShouldBeTrueGivenExpiredAccessToken() {
//        // has expired
//        // where 1671086104 is the accessTokne in EpochSeconds and 400 is the now time we substract
//        Utils.unitTestInstance = Instant.fromEpochSeconds(1671086104 - 400)
//        Utils.hasJwtExpired(accessToken,300).shouldBe(false)
//
//        // has not expired
//        Utils.unitTestInstance = Instant.fromEpochSeconds(1671086104 - 200)
//        Utils.hasJwtExpired(accessToken,300).shouldBe(true)
//    }
//}