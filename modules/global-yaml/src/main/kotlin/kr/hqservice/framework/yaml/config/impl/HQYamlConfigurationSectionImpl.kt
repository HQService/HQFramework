package kr.hqservice.framework.yaml.config.impl

import kr.hqservice.framework.yaml.config.HQYamlConfigurationSection
import org.spongepowered.configurate.ConfigurationNode

open class HQYamlConfigurationSectionImpl(
    private val root: ConfigurationNode
) : HQYamlConfigurationSection {
    private fun findNode(fullPath: String): ConfigurationNode {
        val nodeKeys = fullPath.split('.')
        var pointer = root
        for (element in nodeKeys)
            pointer = pointer.node(element)//getNode(element)
        return pointer
    }

    fun getRoot(): ConfigurationNode {
        return root
    }

    override fun set(key: String, value: Any?) {
        findNode(key).set(value)
    }

    override fun getSection(key: String): HQYamlConfigurationSection? {
        val node = findNode(key)
        return if (node.virtual()) null else HQYamlConfigurationSectionImpl(node)
    }

    override fun getKeys(): List<String> {
        return root.childrenMap().keys.map {
            it.toString()
        }
    }

    override fun contains(key: String): Boolean {
        return !findNode(key).isNull
    }

    override fun getString(key: String, default: String): String {
        return findNode(key).getString(default) ?: default
    }

    override fun findString(key: String): String? {
        return findNode(key).string
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return findNode(key).getBoolean(default)
    }

    override fun findBoolean(key: String): Boolean? {
        return findNode(key).boolean
    }

    override fun getInt(key: String, default: Int): Int {
        return findNode(key).getInt(default)
    }

    override fun findInt(key: String): Int? {
        return findNode(key).int
    }

    override fun getDouble(key: String, default: Double): Double {
        return findNode(key).getDouble(default)
    }

    override fun findDouble(key: String): Double? {
        return findNode(key).double
    }

    override fun getFloat(key: String, default: Float): Float {
        return findNode(key).getFloat(default)
    }

    override fun findFloat(key: String): Float? {
        return findNode(key).float
    }

    override fun getLong(key: String, default: Long): Long {
        return findNode(key).getLong(default)
    }

    override fun findLong(key: String): Long? {
        return findNode(key).long
    }

    override fun getStringList(key: String): List<String> {
        return getList(key, String::class.java)
    }

    override fun getIntegerList(key: String): List<Int> {
        return getList(key, Int::class.javaObjectType)
    }

    override fun getLongList(key: String): List<Long> {
        return getList(key, Long::class.javaObjectType)
    }

    override fun getDoubleList(key: String): List<Double> {
        return getList(key, Double::class.javaObjectType)
    }

    override fun getFloatList(key: String): List<Float> {
        return getList(key, Float::class.javaObjectType)
    }

    private fun <T> getList(key: String, clazz: Class<T>): List<T> {
        return if (!findNode(key).isList) emptyList()
        else findNode(key).getList(clazz) ?: emptyList()
    }
}