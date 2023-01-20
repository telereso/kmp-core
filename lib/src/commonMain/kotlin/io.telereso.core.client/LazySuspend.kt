package io.telereso.core.client

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * https://github.com/Kotlin/kotlinx.coroutines/issues/706
 */
class LazySuspend<T>(initializer: suspend () -> T) {
    private var initializer: (suspend () -> T)? = initializer
    private var mutex: Mutex? = Mutex()
    private var _value: T? = null

    suspend fun value(): T {
        val m = mutex ?: return _value as T // Warning: unchecked cast.
        m.withLock {
            val i = initializer ?: return _value as T // Warning: unchecked cast.
            val v = i()
            _value = v
            initializer = null
            mutex = null
            return v
        }
    }
}