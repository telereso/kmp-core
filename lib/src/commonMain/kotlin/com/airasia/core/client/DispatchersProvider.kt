package com.airasia.core.client

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DispatchersProvider {

    var testDispatcher: CoroutineDispatcher? = null

    val Default
        get() = testDispatcher ?: Dispatchers.Default

    val Main
        get() = testDispatcher ?: Dispatchers.Main

    val Unconfined
        get() = testDispatcher ?: Dispatchers.Unconfined
}