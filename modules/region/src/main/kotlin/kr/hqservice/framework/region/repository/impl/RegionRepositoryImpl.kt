package kr.hqservice.framework.region.repository.impl

import kr.hqservice.framework.region.data.Region
import kr.hqservice.framework.region.repository.RegionRepository
import org.bukkit.plugin.Plugin

class RegionRepositoryImpl(private val plugin: Plugin) : RegionRepository<Int, Region> {

    private val regions = mutableMapOf<Int, Region>()

    fun load() {

    }

    fun save() {

    }

    override fun findById(key: Int): Region? {
        return regions[key]
    }

    override fun getById(key: Int): Region {
        return findById(key) ?: throw IllegalArgumentException()
    }

    override fun findByName(key: String): Region? {
        return regions.filter { it.value.name == key }.values.firstOrNull()
    }

    override fun getByName(key: String): Region {
        return findByName(key) ?: throw IllegalArgumentException()
    }

    override fun contains(key: Int): Boolean {
        return regions.containsKey(key)
    }

    override fun contains(key: String): Boolean {
        return regions.filter { it.value.name == key }.keys.isNotEmpty()
    }

    override fun set(key: Int, value: Region) {
        regions[key] = value
    }

    override fun remove(key: Int) {
        regions.remove(key)
    }

    override fun remove(key: String) {
        val id = regions.filter { it.value.name == key }.keys.firstOrNull() ?: return
        regions.remove(id)
    }

    override fun clear() {
        regions.clear()
    }
}