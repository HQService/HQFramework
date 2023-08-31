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

    override fun getSection(key: String): HQYamlConfigurationSection? {
        val node = findNode(key)
        return if (node.virtual()) null else HQYamlConfigurationSectionImpl(node)
    }

    override fun getString(key: String, default: String): String {
        return findNode(key).getString(default) ?: default
    }

    override fun findString(key: String): String? {
        return findNode(key).string
    }

    override fun getKeys(): List<String> {
        return root.childrenMap().keys.map {
            it.toString()
        }
    }

    override fun getStringList(key: String): List<String> {
        return if (!findNode(key).isList) emptyList()
        else findNode(key).getList(String::class.java) ?: emptyList()
    }

    override fun getIntegerList(key: String): List<Int> {
        return if (!findNode(key).isList) emptyList()
        else findNode(key).getList(Int::class.javaObjectType) ?: emptyList()
    }

    override fun getLongList(key: String): List<Long> {
        return if (!findNode(key).isList) emptyList()
        else findNode(key).getList(Long::class.java) ?: emptyList()
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

    override fun getLong(key: String, default: Long): Long {
        return findNode(key).getLong(default)
    }

    override fun findLong(key: String): Long? {
        return findNode(key).long
    }

    override fun set(key: String, value: Any?) {
        findNode(key).set(value)
    }
}