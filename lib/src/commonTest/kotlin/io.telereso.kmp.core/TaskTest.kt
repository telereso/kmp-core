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
//import io.kotest.matchers.booleans.shouldBeTrue
//import io.kotest.matchers.collections.shouldBeEmpty
//import io.kotest.matchers.collections.shouldContain
//import io.kotest.matchers.collections.shouldNotBeEmpty
//import io.kotest.matchers.nulls.shouldBeNull
//import io.kotest.matchers.nulls.shouldNotBeNull
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.types.shouldNotBeInstanceOf
//import io.telereso.kmp.core.extensions.getOrDefault
//import io.telereso.kmp.core.models.ClientException
//import kotlinx.coroutines.*
//import kotlinx.coroutines.test.TestResult
//import kotlinx.coroutines.test.UnconfinedTestDispatcher
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import kotlin.test.BeforeTest
//import kotlin.test.Test
//
//@OptIn(ExperimentalCoroutinesApi::class)
//abstract class TaskTest {
//    var testDispatcher = UnconfinedTestDispatcher()
//
//    @BeforeTest
//    fun setUp() {
//        // Dispatchers.setMain to override Dispatchers.Main in tests;
//        Dispatchers.setMain(testDispatcher)
//        // Needed to replace Dispatchers used within Coroutines to test based dispatcher.
//        DispatchersProvider.testDispatcher = testDispatcher
//    }
//
//    abstract fun onSuccessOnly(): TestResult
//
//    abstract fun onSuccessAndOnComplete(): TestResult
//
//    abstract fun onSuccessUIOnly(): TestResult
//
//    abstract fun onSuccessUIAndOnCompleteUI(): TestResult
//
//    abstract fun onSuccessDouble(): TestResult
//
//    abstract fun onSuccessAndOnCompleteDouble(): TestResult
//
//    abstract fun onSuccessUIDouble(): TestResult
//
//    abstract fun onSuccessUIAndOnCompleteDouble(): TestResult
//
//    abstract fun onSuccessOnlyWithAwait(): TestResult
//
//    abstract fun onSuccessAndOnCompleteWithAwait(): TestResult
//
//    abstract fun onSuccessOnlyAndHandledException(): TestResult
//
//    abstract fun onSuccessAndOnCompleteAndHandledException(): TestResult
//
//    abstract fun onSuccessOnlyWithAwaitAndHandledException(): TestResult
//
//    abstract fun onSuccessAndOnCompleteWithAwaitAndHandledException(): TestResult
//
//    abstract fun onSuccessWithFailure(): TestResult
//
//    abstract fun onSuccessWithFailureAndOnComplete(): TestResult
//
//    abstract fun onSuccessWithSuccessUI(): TestResult
//
//    abstract fun onSuccessWithSuccessUIAndOnCompleteUI(): TestResult
//
//    abstract fun onSuccessWithFailureWithSuccessUI(): TestResult
//
//    abstract fun onSuccessWithFailureWithSuccessUIAndOnComplete(): TestResult
//
//    abstract fun onSuccessWithFailureWithFailureUIeWithSuccessUI(): TestResult
//
//    abstract fun onSuccessWithFailureWithFailureUIeWithSuccessUIAndOnCompleteUI(): TestResult
//
//    abstract fun onSuccessDoubleInSuccessUIDouble(): TestResult
//
//    abstract fun onSuccessDoubleInSuccessUIAndOnCompleteUIDouble(): TestResult
//
//
/////////////////////////////////////////////////////////////////////////////////////////
//
//    // Test Failure Cases
//    abstract fun onFailureOnly(): TestResult
//
//    abstract fun onFailureAndOnComplete(): TestResult
//
//    abstract fun onFailureUIOnly(): TestResult
//
//    abstract fun onFailureUIAndOnCompleteUI(): TestResult
//
//    abstract fun onFailureDouble(): TestResult
//
//    abstract fun onFailureAndOnCompleteDouble(): TestResult
//
//    abstract fun onFailureUIDouble(): TestResult
//
//    abstract fun onFailureUIAndOnCompleteUIDouble(): TestResult
//
//    abstract fun onFailureOnlyWithAwait(): TestResult
//
//    abstract fun onFailureAndOnCompleteWithAwait(): TestResult
//
//    abstract fun onFailureWithSuccess(): TestResult
//
//    abstract fun onFailureWithSuccessAndOnComplete(): TestResult
//
//    abstract fun onFailureWithFailureUI(): TestResult
//
//    abstract fun onFailureWithFailureUIAndOnCompleteUI(): TestResult
//
//    abstract fun onFailureWithSuccessWithFailureUI(): TestResult
//
//    abstract fun onFailureWithSuccessWithFailureUIAndOnCompleteUI(): TestResult
//
//    abstract fun onFailureWithSuccessWithSuccessUIWithFailureUI(): TestResult
//
//    abstract fun onFailureWithSuccessWithSuccessUIWithFailureUIAndOnComplete(): TestResult
//
//    abstract fun onFailureWithAwaitWithSuccessWithSuccessUIWithFailureUI(): TestResult
//
//    abstract fun onFailureWithAwaitWithSuccessWithSuccessUIWithFailureUIAndOnCompleteUI(): TestResult
//
//    abstract fun onFailureDoubleOnFailureUIDouble(): TestResult
//
//    abstract fun onFailureDoubleOnFailureUIAndOnCompleteUIDouble(): TestResult
//
//    ///////////////////////////////////////////////////////////////////////////////////////
//
//    // Test complete Cases
//
//    abstract fun onCompleteOnly(): TestResult
//
//    abstract fun onCompleteOnlyFailed(): TestResult
//    abstract fun onCompleteOnlyWithDelay(): TestResult
//
//    abstract fun onCompleteOnlyWithDelayFailed(): TestResult
//
//    abstract fun onCompleteOnlyCanceled(): TestResult
//    abstract fun onCompleteOnlyCanceledWithDelay(): TestResult
//
//
//    ///////////////////////////////////////////////////////////////////////////////////////
//
//    // Test Cancel Cases
//    abstract fun onCancel(): TestResult
//
//    abstract fun onCancelAndOnComplete(): TestResult
//
//    abstract fun someFavoriteActressesListTaskShouldInvokeOnSuccessUI(): TestResult
//
//    abstract fun someFavoriteActressesListTaskShouldInvokeOnSuccessUIAndOnCompleteUI(): TestResult
//
//    abstract fun taskShouldInvokeOnFailure(): TestResult
//
//    abstract fun taskShouldInvokeOnFailureAndOnComplete(): TestResult
//
//    abstract fun taskWithRetry(): TestResult
//
//}
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class TaskTestImpl : TaskTest() {
//
//    // Test Success Cases
//    @Test
//    override fun onSuccessOnly() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }
//
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessAndOnComplete() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        var itemsOnComplete: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            itemsOnComplete = res
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//
//        itemsOnComplete.shouldNotBeEmpty()
//        itemsOnComplete?.size.shouldBe(3)
//        itemsOnComplete?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessUIOnly() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccessUI {
//            itemsOnSuccess = it
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessUIAndOnCompleteUI() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        var itemsOnComplete: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccessUI {
//            itemsOnSuccess = it
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            itemsOnComplete = res
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//
//        itemsOnComplete.shouldNotBeEmpty()
//        itemsOnComplete?.size.shouldBe(3)
//        itemsOnComplete?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(3)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessAndOnCompleteDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnComplete = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnComplete.addAll(it)
//            }
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnComplete.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(3)
//        itemsOnSuccess.shouldContain("abc")
//
//        itemsOnComplete.shouldNotBeEmpty()
//        itemsOnComplete.size.shouldBe(3)
//        itemsOnComplete.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(3)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessUIAndOnCompleteDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnComplete = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnComplete.addAll(it)
//            }
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnComplete.addAll(it)
//            }
//        }
//
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(3)
//        itemsOnSuccess.shouldContain("abc")
//
//        itemsOnComplete.shouldNotBeEmpty()
//        itemsOnComplete.size.shouldBe(3)
//        itemsOnComplete.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessOnlyWithAwait() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.await()?.let {
//            it.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessAndOnCompleteWithAwait() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.await()?.let {
//            it.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(9)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessOnlyAndHandledException() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            try {
//                listOf("abc", "abcd", "abcde")
//                throw ClientException(message = "Something Went Crazy")
//            } catch (t: Throwable) {
//                listOf("ClientException", "abcd", "abcde")
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(3)
//        itemsOnSuccess.shouldContain("ClientException")
//    }
//
//    @Test
//    override fun onSuccessAndOnCompleteAndHandledException() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            try {
//                listOf("abc", "abcd", "abcde")
//                throw ClientException(message = "Something Went Crazy")
//            } catch (t: Throwable) {
//                listOf("ClientException", "abcd", "abcde")
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("ClientException")
//    }
//
//    @Test
//    override fun onSuccessOnlyWithAwaitAndHandledException() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            try {
//                listOf("abc", "abcd", "abcde")
//                throw ClientException(message = "Something Went Crazy")
//            } catch (t: Throwable) {
//                listOf("ClientException", "abcd", "abcde")
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.awaitOrNull()?.let {
//            it.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("ClientException")
//    }
//
//    @Test
//    override fun onSuccessAndOnCompleteWithAwaitAndHandledException() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            try {
//                listOf("abc", "abcd", "abcde")
//                throw ClientException(message = "Something Went Crazy")
//            } catch (t: Throwable) {
//                listOf("ClientException", "abcd", "abcde")
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.awaitOrNull()?.let {
//            it.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(9)
//        itemsOnSuccess.shouldContain("ClientException")
//    }
//
//    @Test
//    override fun onSuccessWithFailure() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onFailure {
//            itemOnFailure = it
//        }
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessWithFailureAndOnComplete() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        var itemOnFailure: Throwable? = null
//        var itemOnComplete: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onFailure {
//            itemOnFailure = it
//        }.onComplete { res, e ->
//            e.shouldBeNull()
//            itemOnComplete = res
//        }
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//
//        itemOnComplete.shouldNotBeEmpty()
//        itemOnComplete?.size.shouldBe(3)
//        itemOnComplete?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessWithSuccessUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessWithSuccessUIAndOnCompleteUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(9)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessWithFailureWithSuccessUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemOnFailure = it
//        }
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    override fun onSuccessWithFailureWithSuccessUIAndOnComplete() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemOnFailure = it
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(9)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//
//    @Test
//    override fun onSuccessWithFailureWithFailureUIeWithSuccessUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemOnFailure = it
//        }.onFailureUI {
//            itemOnFailure = it
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessWithFailureWithFailureUIeWithSuccessUIAndOnCompleteUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemOnFailure = it
//        }.onFailureUI {
//            itemOnFailure = it
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//
//        itemOnFailure.shouldBeNull()
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(9)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessDoubleInSuccessUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(6)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
//    @Test
//    override fun onSuccessDoubleInSuccessUIAndOnCompleteUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            res?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess.size.shouldBe(9)
//        itemsOnSuccess.shouldContain("abc")
//    }
//
/////////////////////////////////////////////////////////////////////////////////////////
//
//    // Test Failure Cases
//    @Test
//    override fun onFailureOnly() = runTest {
//        val itemsOnSuccess: List<String> = listOf()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemOnFailure = it
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemOnFailure.shouldNotBeNull()
//        itemOnFailure?.message.shouldBe("Something Went Crazy")
//
//    }
//
//    @Test
//    override fun onFailureAndOnComplete() = runTest {
//        val itemsOnSuccess: List<String> = listOf()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemOnFailure = it
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            e.shouldNotBeNull()
//            e.message.shouldBe("Something Went Crazy")
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemOnFailure.shouldNotBeNull()
//        itemOnFailure?.message.shouldBe("Something Went Crazy")
//
//    }
//
//    @Test
//    override fun onFailureUIOnly() = runTest {
//        val itemsOnSuccess: List<String> = listOf()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailureUI {
//            itemOnFailure = it
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemOnFailure.shouldNotBeNull()
//        itemOnFailure?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun onFailureUIAndOnCompleteUI() = runTest {
//        val itemsOnSuccess: List<String> = listOf()
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailureUI {
//            itemOnFailure = it
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            e.shouldNotBeNull()
//            e.message.shouldBe("Something Went Crazy")
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemOnFailure.shouldNotBeNull()
//        itemOnFailure?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun onFailureDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureAndOnCompleteDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureUIAndOnCompleteUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureOnlyWithAwait() = runTest {
//        val itemsOnSuccess: List<String> = listOf()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureAndOnCompleteWithAwait() = runTest {
//        val itemsOnSuccess: List<String> = listOf()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithSuccess() = runTest {
//        var itemsOnSuccess: List<String>? = null
//        var itemOnFailure: Throwable? = null
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onFailure {
//            itemOnFailure = it
//        }
//
//        itemsOnSuccess.shouldBeNull()
//        itemOnFailure.shouldNotBeNull()
//        itemOnFailure?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun onFailureWithSuccessAndOnComplete() = runTest {
//        var itemsOnSuccess: List<String>? = null
//        var itemOnFailure: Throwable? = null
//        var itemOnComplete: Throwable? = null
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onFailure {
//            itemOnFailure = it
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            itemOnComplete = e
//
//        }
//
//        itemsOnSuccess.shouldBeNull()
//        itemOnFailure.shouldNotBeNull()
//        itemOnFailure?.message.shouldBe("Something Went Crazy")
//
//        itemOnComplete.shouldNotBeNull()
//        itemOnComplete?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun onFailureWithFailureUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithFailureUIAndOnCompleteUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(3)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithSuccessWithFailureUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeNull()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithSuccessWithFailureUIAndOnCompleteUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeNull()
//        itemsOnFailure.size.shouldBe(3)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//
//    @Test
//    override fun onFailureWithSuccessWithSuccessUIWithFailureUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithSuccessWithSuccessUIWithFailureUIAndOnComplete() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onCompleteUI { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(3)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithAwaitWithSuccessWithSuccessUIWithFailureUI() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onSuccess {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onSuccessUI {
//            it?.let {
//                itemsOnSuccess.addAll(it)
//            }
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureWithAwaitWithSuccessWithSuccessUIWithFailureUIAndOnCompleteUI() =
//        runTest {
//            val itemsOnSuccess = mutableListOf<String>()
//            val itemsOnFailure = mutableListOf<Throwable>()
//
//            Task.execute<List<String>?> {
//                throw ClientException(message = "Something Went Crazy")
//            }.onSuccess {
//                it?.let {
//                    itemsOnSuccess.addAll(it)
//                }
//            }.onSuccessUI {
//                it?.let {
//                    itemsOnSuccess.addAll(it)
//                }
//            }.onFailure {
//                itemsOnFailure.add(it)
//            }.onFailureUI {
//                itemsOnFailure.add(it)
//            }.onCompleteUI { res, e ->
//                res.shouldBeNull()
//                itemsOnFailure.add(e!!)
//            }.awaitOrNull()
//
//            itemsOnSuccess.shouldBeEmpty()
//            itemsOnFailure.shouldNotBeEmpty()
//            itemsOnFailure.size.shouldBe(3)
//            itemsOnFailure.forEach {
//                it.message.shouldBe("Something Went Crazy")
//            }
//        }
//
//    @Test
//    override fun onFailureDoubleOnFailureUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailure {
//            itemsOnFailure.add(it)
//
//        }.onFailure {
//            itemsOnFailure.add(it)
//
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onFailureDoubleOnFailureUIAndOnCompleteUIDouble() = runTest {
//        val itemsOnSuccess = mutableListOf<String>()
//        val itemsOnFailure = mutableListOf<Throwable>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailure {
//            itemsOnFailure.add(it)
//
//        }.onFailure {
//            itemsOnFailure.add(it)
//
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldNotBeEmpty()
//        itemsOnFailure.size.shouldBe(2)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("Something Went Crazy")
//        }
//    }
//
//    @Test
//    override fun onCompleteOnly() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onComplete { res, _ ->
//            itemsOnSuccess = res
//        }
//
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onCompleteOnlyFailed() = runTest {
//        var itemsOnSuccess: List<String>? = null
//        val itemsOnFailure = mutableListOf<Throwable?>()
//
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onComplete { res, e ->
//            itemsOnSuccess = res
//            itemsOnFailure.add(e)
//        }
//
//        itemsOnSuccess.shouldBeNull()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure[0]?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun onCompleteOnlyCanceled() = runTest {
//        var itemsOnSuccess: List<String>? = null
//        val itemsOnFailure = mutableListOf<Throwable?>()
//
//        Task.execute<List<String>?> {
//            delay(100)
//            listOf()
//        }.onComplete { res, e ->
//            itemsOnSuccess = res
//            itemsOnFailure.add(e)
//        }.apply {
//            cancel(message = "cancelled")
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeNull()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure[0]?.message.shouldBe("cancelled")
//    }
//
//    @Test
//    override fun onCompleteOnlyWithDelay() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            delay(100)
//            listOf("abc", "abcd", "abcde")
//        }.onComplete { res, _ ->
//            itemsOnSuccess = res
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abc")
//    }
//
//    @Test
//    override fun onCompleteOnlyWithDelayFailed() = runTest {
//        var itemsOnSuccess: List<String>? = null
//        val itemsOnFailure = mutableListOf<Throwable?>()
//
//        Task.execute<List<String>?> {
//            delay(100)
//            throw ClientException(message = "Something Went Crazy")
//        }.onComplete { res, e ->
//            itemsOnSuccess = res
//            itemsOnFailure.add(e)
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeNull()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure[0]?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun onCompleteOnlyCanceledWithDelay() = runTest {
//        var itemsOnSuccess: List<String>? = null
//        val itemsOnFailure = mutableListOf<Throwable?>()
//
//        Task.execute<List<String>?> {
//            delay(200)
//            listOf()
//        }.onComplete { res, e ->
//            itemsOnSuccess = res
//            itemsOnFailure.add(e)
//        }.apply {
//            delay(100)
//            cancel(message = "cancelled")
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeNull()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure[0]?.message.shouldBe("cancelled")
//    }
//
//    @Test
//    override fun onCancel() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        val itemsOnFailure = mutableListOf<Throwable>()
//        var isCanceled: Throwable? = null
//
//        Task.execute<List<String>?> {
//            delay(100)
//            listOf("Zendaya Maree", "Chloë Grace Moretz", "Luna Blaise")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onSuccessUI {
//            itemsOnSuccess = it
//        }.onCancel {
//            isCanceled = it
//        }.apply {
//            cancel(message = "cancelled")
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeEmpty()
//        itemsOnFailure.shouldBeEmpty()
//        isCanceled.shouldNotBeNull()
//    }
//
//    @Test
//    override fun onCancelAndOnComplete() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        val itemsOnFailure = mutableListOf<Throwable>()
//        var isCanceled: Throwable? = null
//
//        Task.execute<List<String>?> {
//            delay(100)
//            listOf("Zendaya Maree", "Chloë Grace Moretz", "Luna Blaise")
//        }.onFailure {
//            itemsOnFailure.add(it)
//        }.onFailureUI {
//            itemsOnFailure.add(it)
//        }.onSuccessUI {
//            itemsOnSuccess = it
//        }.onCancel {
//            isCanceled = it
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            itemsOnFailure.add(e!!)
//        }.apply {
//            cancel(message = "cancelled")
//        }.awaitOrNull()
//
//        itemsOnSuccess.shouldBeEmpty()
//        isCanceled.shouldNotBeNull()
//        itemsOnFailure.size.shouldBe(1)
//        itemsOnFailure.forEach {
//            it.message.shouldBe("cancelled")
//        }
//    }
//
//    @Test
//    override fun someFavoriteActressesListTaskShouldInvokeOnSuccessUI() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccessUI {
//            itemsOnSuccess = it
//        }.onFailureUI {
//        }
//
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abcde")
//    }
//
//    @Test
//    override fun someFavoriteActressesListTaskShouldInvokeOnSuccessUIAndOnCompleteUI() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        var itemsOnComplete: List<String>? = listOf()
//
//        Task.execute<List<String>?> {
//            listOf("abc", "abcd", "abcde")
//        }.onSuccessUI {
//            itemsOnSuccess = it
//        }.onFailureUI {
//        }.onCompleteUI { res, e ->
//            e.shouldBeNull()
//            itemsOnComplete = res
//        }
//
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abcde")
//
//        itemsOnComplete?.size.shouldBe(3)
//        itemsOnComplete?.shouldContain("abcde")
//    }
//
//
//    @Test
//    override fun taskShouldInvokeOnFailure() = runTest {
//        var itemsFail: ClientException? = null
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsFail = it
//        }
//        itemsFail?.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun taskShouldInvokeOnFailureAndOnComplete() = runTest {
//        var itemsFail: ClientException? = null
//        var itemsComplete: ClientException? = null
//        Task.execute<List<String>?> {
//            throw ClientException(message = "Something Went Crazy")
//        }.onFailure {
//            itemsFail = it
//        }.onComplete { res, e ->
//            res.shouldBeNull()
//            itemsComplete = e
//        }
//
//        itemsFail!!.message.shouldBe("Something Went Crazy")
//        itemsComplete!!.message.shouldBe("Something Went Crazy")
//    }
//
//    @Test
//    override fun taskWithRetry() = runTest {
//        var itemsOnSuccess: List<String>? = listOf()
//        var failureCount = 0
//        var count = 0
//        Task.execute<List<String>?>(retry = 2) {
//            if (count < 2) {
//                count++
//                throw Throwable("taskWithRetry")
//            }
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onFailure {
//            failureCount = it.failureCount.getOrDefault()
//        }
//
//        count.shouldBe(2)
//        itemsOnSuccess.shouldNotBeEmpty()
//        itemsOnSuccess?.size.shouldBe(3)
//        itemsOnSuccess?.shouldContain("abcde")
//
//        count = 0
//        Task.execute<List<String>?>(retry = 1) {
//            if (count < 2) {
//                count++
//                throw Throwable("taskWithRetry")
//            }
//            listOf("abc", "abcd", "abcde")
//        }.onSuccess {
//            itemsOnSuccess = it
//        }.onFailure {
//            failureCount = it.failureCount.getOrDefault()
//        }
//
//        failureCount.shouldBe(2)
//
//    }
//}