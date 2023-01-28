package io.telereso.kmp.core

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.telereso.kmp.core.models.ClientException
import kotlinx.coroutines.*
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
        // Needed to replace Dispatchers used within Coroutines to test based dispatcher.
        DispatchersProvider.testDispatcher = testDispatcher
    }

    // Test Success Cases
    @Test
    fun onSuccessOnly() = runTest {
        var itemsOnSuccess: List<String>? = listOf()

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            itemsOnSuccess = it
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess?.size.shouldBe(3)
        itemsOnSuccess?.shouldContain("abc")
    }

    @Test
    fun onSuccessUIOnly() = runTest {
        var itemsOnSuccess: List<String>? = listOf()

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccessUI {
            itemsOnSuccess = it
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess?.size.shouldBe(3)
        itemsOnSuccess?.shouldContain("abc")
    }

    @Test
    fun onSuccessDouble() = runTest {
        val itemsOnSuccess = mutableListOf<String>()

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(3)
        itemsOnSuccess.shouldContain("abc")
    }

    @Test
    fun onSuccessUIDouble() = runTest {
        val itemsOnSuccess = mutableListOf<String>()

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(3)
        itemsOnSuccess.shouldContain("abc")
    }

    @Test
    fun onSuccessOnlyWithAwait() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.await()?.let {
            it.let {
                itemsOnSuccess.addAll(it)
            }
        }

        itemOnFailure.shouldBeNull()
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(6)
        itemsOnSuccess.shouldContain("abc")
    }

    @Test
    fun onSuccessOnlyAndHandledException() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            try {
                listOf("abc", "abcd", "abcde")
                throw ClientException(message = "Something Went Crazy")
            } catch (t: Throwable) {
                listOf("ClientException", "abcd", "abcde")
            }
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }

        itemOnFailure.shouldBeNull()
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(3)
        itemsOnSuccess.shouldContain("ClientException")
    }
    @Test
    fun onSuccessOnlyWithAwaitAndHandledException() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            try {
                listOf("abc", "abcd", "abcde")
                throw ClientException(message = "Something Went Crazy")
            } catch (t: Throwable) {
                listOf("ClientException", "abcd", "abcde")
            }
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.await()?.let {
            it.let {
                itemsOnSuccess.addAll(it)
            }
        }

        itemOnFailure.shouldBeNull()
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(6)
        itemsOnSuccess.shouldContain("ClientException")
    }

    @Test
    fun onSuccessWithFailure() = runTest {
        var itemsOnSuccess: List<String>? = listOf()
        var itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            itemsOnSuccess = it
        }.onFailure {
            itemOnFailure = it
        }
        itemOnFailure.shouldBeNull()
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess?.size.shouldBe(3)
        itemsOnSuccess?.shouldContain("abc")
    }

    @Test
    fun onSuccessWithSuccessUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(6)
        itemsOnSuccess.shouldContain("abc")
    }

    @Test
    fun onSuccessWithFailureWithSuccessUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        var itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onFailure {
            itemOnFailure = it
        }
        itemOnFailure.shouldBeNull()
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(6)
        itemsOnSuccess.shouldContain("abc")
    }


    @Test
    fun onSuccessWithFailureWithFailureUIeWithSuccessUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        var itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onFailure {
            itemOnFailure = it
        }.onFailureUI {
            itemOnFailure = it
        }

        itemOnFailure.shouldBeNull()
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(6)
        itemsOnSuccess.shouldContain("abc")
    }

    @Test
    fun onSuccessDoubleInSuccessUIDouble() = runTest {
        val itemsOnSuccess = mutableListOf<String>()

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }
        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess.size.shouldBe(6)
        itemsOnSuccess.shouldContain("abc")
    }


///////////////////////////////////////////////////////////////////////////////////////

    // Test Failure Cases
    @Test
    fun onFailureOnly() = runTest {
        val itemsOnSuccess: List<String> = listOf()
        var itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemOnFailure = it
        }

        itemsOnSuccess.shouldBeEmpty()
        itemOnFailure.shouldNotBeNull()
        itemOnFailure?.message.shouldBe("Something Went Crazy")

    }

    @Test
    fun onFailureUIOnly() = runTest {
        val itemsOnSuccess: List<String> = listOf()
        var itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailureUI {
            itemOnFailure = it
        }

        itemsOnSuccess.shouldBeEmpty()
        itemOnFailure.shouldNotBeNull()
        itemOnFailure?.message.shouldBe("Something Went Crazy")
    }

    @Test
    fun onFailureDouble() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailure {
            itemsOnFailure.add(it)
        }

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.size.shouldBe(1)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    fun onFailureUIDouble() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailureUI {
            itemsOnFailure.add(it)
        }.onFailureUI {
            itemsOnFailure.add(it)
        }

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.size.shouldBe(1)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    fun onFailureOnlyWithAwait() = runTest {
        val itemsOnSuccess: List<String> = listOf()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsOnFailure.add(it)
        }.await()

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(1)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    fun onFailureWithSuccess() = runTest {
        var itemsOnSuccess: List<String>? = null
        var itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onSuccess {
            itemsOnSuccess = it
        }.onFailure {
            itemOnFailure = it
        }

        itemsOnSuccess.shouldBeNull()
        itemOnFailure.shouldNotBeNull()
        itemOnFailure?.message.shouldBe("Something Went Crazy")
    }

    @Test
    fun onFailureWithFailureUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailureUI {
            itemsOnFailure.add(it)
        }

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(2)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    fun onFailureWithSuccessWithFailureUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailureUI {
            itemsOnFailure.add(it)
        }

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeNull()
        itemsOnFailure.size.shouldBe(2)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }


    @Test
    fun onFailureWithSuccessWithSuccessUIWithFailureUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailureUI {
            itemsOnFailure.add(it)
        }

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(2)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    fun onFailureWithAwaitWithSuccessWithSuccessUIWithFailureUI() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onSuccessUI {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailureUI {
            itemsOnFailure.add(it)
        }.await()

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(2)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    fun onFailureDoubleOnFailureUIDouble() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailure {
            itemsOnFailure.add(it)

        }.onFailure {
            itemsOnFailure.add(it)

        }

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(1)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }


    @Test
    fun onCancel() = runTest {
        var itemsOnSuccess: List<String>? = listOf()
        val itemsOnFailure = mutableListOf<Throwable>()
        var isCanceled: Throwable? = null

        Task.execute<List<String>?> {
            cancel(message = "cancelled")
            delay(100)
            listOf("Zendaya Maree", "Chloë Grace Moretz", "Luna Blaise")
        }.onFailure {
            itemsOnFailure.add(it)
        }.onFailureUI {
            itemsOnFailure.add(it)
        }.onSuccessUI {
            itemsOnSuccess = it
        }.onCancel {
            isCanceled = it
        }
        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldBeEmpty()
        isCanceled.shouldNotBeNull()
    }

    @Test
    fun someFavoriteActressesListTaskShouldInvokeOnSuccessUI() = runTest {
        var itemsOnSuccess: List<String>? = listOf()
        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccessUI {
            itemsOnSuccess = it
        }.onFailureUI {
        }

        itemsOnSuccess.shouldNotBeEmpty()
        itemsOnSuccess?.size.shouldBe(3)
        itemsOnSuccess?.shouldContain("abcde")
    }


    @Test
    fun taskShouldInvokeOnFailure() = runTest {
        var itemsFail: ClientException? = null
        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsFail = it
        }
        itemsFail?.message.shouldBe("Something Went Crazy")
    }
}