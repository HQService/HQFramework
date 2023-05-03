package kr.hqservice.framework.region.repository

interface RegionRepository<K, V> {

    fun findById(key: K): V?

    fun getById(key: K): V

    fun findByName(key: String): V?

    fun getByName(key: String): V

    fun contains(key: K): Boolean

    fun contains(key: String): Boolean

    fun set(key: K, value: V)

    fun remove(key: K)

    fun remove(key: String)

    fun clear()
}