package io.telereso.kmp.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class SimpleModelDatabase<T>(
    private val settings: Settings,
    private val keyName: String,
    private val modelSerializer: KSerializer<T>,
    private val getId: (T) -> String,
) {
    private val flow: MutableStateFlow<MutableMap<String, T>> by lazy {
        val json = settings.getString(keyName, "{}")
        val map = Http.ktorConfigJson.decodeFromString(mapSerializer, json).toMutableMap()
        MutableStateFlow(map)
    }
    private val mutex = Mutex()
    private val mapSerializer = MapSerializer(String.serializer(), modelSerializer)

    private fun saveFlow() {
        settings[keyName] = Http.ktorConfigJson.encodeToString(mapSerializer, flow.value)
    }

    private suspend fun addAndSave(list: List<T>, map: MutableMap<String, T>){
        list.forEach { item ->
            map[getId(item)] = item
        }
        flow.emit(map)
        saveFlow()
    }

    /**
     * Only use publicly
     */
    suspend fun getFlow() = mutex.withLock { flow }

    suspend fun add(item: T) = mutex.withLock {
        val map = flow.value.toMutableMap()
        map[getId(item)] = item
        flow.emit(map)
        saveFlow()
    }

    suspend fun add(list: List<T>) = mutex.withLock {
        addAndSave(list, flow.value)
    }

    suspend fun set(list: List<T>) = mutex.withLock {
        addAndSave(list, mutableMapOf())
    }

    suspend fun get(id: String): T? = mutex.withLock {
        return flow.value[id]
    }

    suspend fun clear() = mutex.withLock {
        set(emptyList())
    }
}