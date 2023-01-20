package io.telereso.core.client

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommonFlowTest {

    var testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        // Dispatchers.setMain to override Dispatchers.Main in tests;
        Dispatchers.setMain(testDispatcher)
        // Needed to replace Dispatchers used within Coruitiunes to test based dispatcher.
        DispatchersProvider.testDispatcher = testDispatcher
    }


    @Test
    fun shouldCommonFlow() = runTest {
        getTestsFlow().collect {
            it.shouldBe("Hello Common Flow")
        }
    }

    private fun getTestsFlow(): CommonFlow<String> {
        val abcFlow: Flow<String> = flow {
            emit("Hello Common Flow")
        }
        return abcFlow.asCommonFlow()
    }

    @Test
    fun exceptionShouldBeNullOnEach()= runTest {
        var value:String?= null
        var fail:ClientException? = null
        val abcFlow: Flow<String> = flow {
           emit("Some")
        }
        abcFlow.asCommonFlow().watch { s, exception ->
            value = s
            fail = exception
        }
        value.shouldBe("Some")
        fail.shouldBeNull()
        fail?.message.shouldBe(null)
    }

    @Test
    @Throws(ClientException::class)
    fun throwFlowError()= runTest {
        var value:String?= null
        var fail:ClientException? = null
        val abcFlow: Flow<String> = flow {
            throw ClientException(message = "Pay your Flow Bill to Resume")
        }
        abcFlow.asCommonFlow().watch { s, exception ->
            value = s
            fail = exception
        }
        value.shouldBe(null)
        fail.shouldNotBeNull()
        fail?.message.shouldBe("Pay your Flow Bill to Resume")
    }

    @Test
    @Throws(Throwable::class)
    fun throwFlowErrorThrowable()= runTest {
        var value:String?= null
        var fail:Throwable? = null
        val abcFlow: Flow<String> = flow {
            throw Throwable(message = "Pay your Flow Bill to Resume")
        }
        abcFlow.asCommonFlow().watch { s, exception ->
            value = s
            fail = exception
        }
        value.shouldBe(null)
        fail.shouldBeNull()
        fail?.message.shouldBe(null)
    }
}