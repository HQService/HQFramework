package kr.hqservice.framework.database.component.repository

import java.util.concurrent.ConcurrentHashMap

abstract class HQMemoryRepository<K, V>(
    protected val dataMap: MutableMap<K, V> = ConcurrentHashMap<K, V>()
) : HQRepository, MutableMap<K, V> by dataMap