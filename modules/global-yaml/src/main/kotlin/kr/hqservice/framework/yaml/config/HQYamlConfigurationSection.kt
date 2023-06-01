package kr.hqservice.framework.yaml.config

interface HQYamlConfigurationSection {
    fun getString(key: String): String

    fun getBoolean(key: String): Boolean

    fun getInt(key: String): Int

    fun getDouble(key: String): Double

    fun getLong(key: String): Long

    fun getSection(key: String): HQYamlConfigurationSection?
}