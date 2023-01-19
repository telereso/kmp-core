package com.airasia.core.client

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskTest {

    var testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        // Dispatchers.setMain to override Dispatchers.Main in tests;
        Dispatchers.setMain(testDispatcher)
        // Needed to replace Dispatchers used within Coruitiunes to test based dispatcher.
        DispatchersProvider.testDispatcher = testDispatcher
    }

    @Test
    fun someFavoriteActressesListTaskShouldInvokeOnSuccess() = runTest {
        var itemsOnSuccess: List<String>? = listOf()
        Task<List<String>?>().onSuccess {
            itemsOnSuccess = it
        }.onFailure {

        }.apply {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                failure?.invoke(exception.toClientException())
            }
            scope.launch(coroutineExceptionHandler) {
                success?.invoke(listOf("Zendaya Maree","Chloë Grace Moretz","Luna Blaise"))
            }
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess?.size.shouldBe(3)
        itemsOnSuccess?.shouldContain("Chloë Grace Moretz")
    }

    @Test
    fun shouldCancel() = runTest {
        var itemsOnSuccess: List<String>? = listOf()
        Task<List<String>?>().onSuccessUI {
            itemsOnSuccess = it
        }.apply {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                failure?.invoke(exception.toClientException())
            }
            scope.launch(coroutineExceptionHandler) {
                cancel(message = "cancelled")
                delay(100)
                success?.invoke(listOf("Zendaya Maree", "Chloë Grace Moretz", "Luna Blaise"))
            }
        }
        itemsOnSuccess.shouldBeEmpty()
    }

    @Test
    fun someFavoriteActressesListTaskShouldInvokeOnSuccessUI() = runTest {
        var itemsOnSuccess: List<String>? = listOf()
        Task<List<String>?>().onSuccessUI {
            itemsOnSuccess = it
        }.onFailureUI {
        }.apply {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                failure?.invoke(exception.toClientException())
            }
            scope.launch(coroutineExceptionHandler) {
                success?.invoke(listOf("Zendaya Maree", "Chloë Grace Moretz", "Luna Blaise"))
            }
        }

        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess?.size.shouldBe(3)
        itemsOnSuccess?.shouldContain("Chloë Grace Moretz")
    }


    @Test
    fun taskShouldInvokeOnFailure() = runTest {
        var itemsFail: ClientException? = null
        Task<List<String>?>().onFailure {
            itemsFail = it
        }.apply {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                failure?.invoke(exception.toClientException())
            }
            scope.launch(coroutineExceptionHandler) {
                throw ClientException(message = "Something Went Crazy")
            }
        }
        itemsFail?.message.shouldBe("Something Went Crazy")
    }


    @Test
    fun taskShouldInvokeOnFailureUIUI() = runTest {
        var itemsFail:ClientException? = null
        Task<List<String>?>().onFailureUI {
            itemsFail = it
        }.apply {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                failure?.invoke(exception.toClientException())
            }
            scope.launch(coroutineExceptionHandler) {
                throw ClientException(message = "Something Went Crazy")
            }
        }
        itemsFail?.message.shouldBe("Something Went Crazy")
    }

}