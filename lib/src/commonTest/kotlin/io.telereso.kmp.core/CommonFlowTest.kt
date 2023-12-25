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
//import io.kotest.matchers.nulls.shouldBeNull
//import io.kotest.matchers.nulls.shouldNotBeNull
//import io.kotest.matchers.shouldBe
//import io.telereso.kmp.core.models.ClientException
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.test.UnconfinedTestDispatcher
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import kotlin.test.BeforeTest
//import kotlin.test.Ignore
//import kotlin.test.Test
//
//@OptIn(ExperimentalCoroutinesApi::class)
//@Ignore
//class CommonFlowTest {
//
//    var testDispatcher = UnconfinedTestDispatcher()
//
//    @BeforeTest
//    fun setUp() {
//        // Dispatchers.setMain to override Dispatchers.Main in tests;
//        Dispatchers.setMain(testDispatcher)
//        // Needed to replace Dispatchers used within Coruitiunes to test based dispatcher.
//        DispatchersProvider.testDispatcher = testDispatcher
//    }
//
//
//    @Test
//    fun shouldCommonFlow() = runTest {
//        getTestsFlow().collect {
//            it.shouldBe("Hello Common Flow")
//        }
//    }
//
//    private fun getTestsFlow(): CommonFlow<String> {
//        val abcFlow: Flow<String> = flow {
//            emit("Hello Common Flow")
//        }
//        return abcFlow.asCommonFlow()
//    }
//
//    @Test
//    fun exceptionShouldBeNullOnEach()= runTest {
//        var value:String?= null
//        var fail: ClientException? = null
//        val abcFlow: Flow<String> = flow {
//           emit("Some")
//        }
//        abcFlow.asCommonFlow().watch { s, exception ->
//            value = s
//            fail = exception
//        }
//        value.shouldBe("Some")
//        fail.shouldBeNull()
//        fail?.message.shouldBe(null)
//    }
//
//    @Test
//    @Throws(ClientException::class)
//    fun throwFlowError()= runTest {
//        var value:String?= null
//        var fail:ClientException? = null
//        val abcFlow: Flow<String> = flow {
//            throw ClientException(message = "Pay your Flow Bill to Resume")
//        }
//        abcFlow.asCommonFlow().watch { s, exception ->
//            value = s
//            fail = exception
//        }
//        value.shouldBe(null)
//        fail.shouldNotBeNull()
//        fail?.message.shouldBe("Pay your Flow Bill to Resume")
//    }
//
//    @Test
//    @Throws(Throwable::class)
//    fun throwFlowErrorThrowable()= runTest {
//        var value:String?= null
//        var fail:Throwable? = null
//        val abcFlow: Flow<String> = flow {
//            throw Throwable(message = "Pay your Flow Bill to Resume")
//        }
//        abcFlow.asCommonFlow().watch { s, exception ->
//            value = s
//            fail = exception
//        }
//        value.shouldBe(null)
//        fail.shouldBeNull()
//        fail?.message.shouldBe(null)
//    }
//}