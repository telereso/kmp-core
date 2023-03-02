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
class TaskAndroidTest : TaskTest()  {

    // Test Success Cases
    @Test
    override fun onSuccessOnly() = runTest {
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
    override fun onSuccessUIOnly() = runTest {
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
    override fun onSuccessDouble() = runTest {
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
    override fun onSuccessUIDouble() = runTest {
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
    override fun onSuccessOnlyWithAwait() = runTest {
        val itemsOnSuccess = mutableListOf<String>()
        val itemOnFailure: Throwable? = null

        Task.execute<List<String>?> {
            listOf("abc", "abcd", "abcde")
        }.onSuccess {
            it?.let {
                itemsOnSuccess.addAll(it)
            }
        }.awaitOrNull()?.let {
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
    override fun onSuccessOnlyAndHandledException() = runTest {
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
    override fun onSuccessOnlyWithAwaitAndHandledException() = runTest {
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
        }.awaitOrNull()?.let {
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
    override fun onSuccessWithFailure() = runTest {
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
    override fun onSuccessWithSuccessUI() = runTest {
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
    override fun onSuccessWithFailureWithSuccessUI() = runTest {
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
    override fun onSuccessWithFailureWithFailureUIeWithSuccessUI() = runTest {
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
    override fun onSuccessDoubleInSuccessUIDouble() = runTest {
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
    override fun onFailureOnly() = runTest {
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
    override fun onFailureUIOnly() = runTest {
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
    override fun onFailureDouble() = runTest {
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
    override fun onFailureUIDouble() = runTest {
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
    override fun onFailureOnlyWithAwait() = runTest {
        val itemsOnSuccess: List<String> = listOf()
        val itemsOnFailure = mutableListOf<Throwable>()

        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsOnFailure.add(it)
        }.awaitOrNull()

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(1)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    override fun onFailureWithSuccess() = runTest {
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
    override fun onFailureWithFailureUI() = runTest {
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
    override fun onFailureWithSuccessWithFailureUI() = runTest {
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
    override fun onFailureWithSuccessWithSuccessUIWithFailureUI() = runTest {
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
    override fun onFailureWithAwaitWithSuccessWithSuccessUIWithFailureUI() = runTest {
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
        }.awaitOrNull()

        itemsOnSuccess.shouldBeEmpty()
        itemsOnFailure.shouldNotBeEmpty()
        itemsOnFailure.size.shouldBe(2)
        itemsOnFailure.forEach {
            it.message.shouldBe("Something Went Crazy")
        }
    }

    @Test
    override fun onFailureDoubleOnFailureUIDouble() = runTest {
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
    override fun onCancel() = runTest {
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
    override fun someFavoriteActressesListTaskShouldInvokeOnSuccessUI() = runTest {
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
    override fun taskShouldInvokeOnFailure() = runTest {
        var itemsFail: ClientException? = null
        Task.execute<List<String>?> {
            throw ClientException(message = "Something Went Crazy")
        }.onFailure {
            itemsFail = it
        }
        itemsFail?.message.shouldBe("Something Went Crazy")
    }
}