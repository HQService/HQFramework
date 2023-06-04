package kr.hqservice.framework.yaml.config

interface HQYamlConfigurationSection {
    fun getString(key: String): String

    fun getStringList(key: String): List<String>

    fun getIntegerList(key: String): List<Int>

    fun getBoolean(key: String): Boolean

    fun getInt(key: String): Int

    fun getDouble(key: String): Double

    fun getLong(key: String): Long

    fun set(key: String, value: Any?)

    fun getSection(key: String): HQYamlConfigurationSection?
}