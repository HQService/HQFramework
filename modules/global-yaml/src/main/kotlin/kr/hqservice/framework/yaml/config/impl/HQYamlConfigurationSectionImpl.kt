package kr.hqservice.framework.yaml.config.impl

import kr.hqservice.framework.yaml.config.HQYamlConfigurationSection
import ninja.leaping.configurate.ConfigurationNode

open class HQYamlConfigurationSectionImpl(
    private val root: ConfigurationNode
) : HQYamlConfigurationSection {
    private fun findNode(fullPath: String): ConfigurationNode {
        val nodeKeys = fullPath.split('.')
        var pointer = root
        for(element in nodeKeys)
            pointer = pointer.getNode(element)
        return pointer
    }

    fun getRoot(): ConfigurationNode {
        return root
    }

    override fun getSection(key: String): HQYamlConfigurationSection? {
        return findNode(key).run { HQYamlConfigurationSectionImpl(this) }
    }

    override fun getString(key: String): String {
        return findNode(key).getString("") ?: ""
    }

    override fun getKeys(): List<String> {
        return root.childrenMap.keys.map {
            it.toString()
        }
    }

    override fun getStringList(key: String): List<String> {
        return if(!findNode(key).isList) emptyList()
        else findNode(key).getList { it.toString() }
    }

    override fun getIntegerList(key: String): List<Int> {
        return if(!findNode(key).isList) emptyList()
        else findNode(key).getList {
            if (it is String) it.toInt()
            else it as Int
        }
    }

    override fun getBoolean(key: String): Boolean {
        return findNode(key).getBoolean(false)
    }

    override fun getInt(key: String): Int {
        return findNode(key).getInt(0)
    }

    override fun getDouble(key: String): Double {
        return findNode(key).getDouble(.0)
    }

    override fun getLong(key: String): Long {
        return findNode(key).getLong(0)
    }

    override fun set(key: String, value: Any?) {
        findNode(key).value = value
    }
}