package kr.hqservice.framework.yaml.config

interface HQYamlConfigurationSection {
    fun set(key: String, value: Any?)

    fun getSection(key: String): HQYamlConfigurationSection?

    fun getKeys(): List<String>

    fun contains(key: String): Boolean

    fun getString(key: String, default: String = ""): String

    fun findString(key: String): String?

    fun getBoolean(key: String, default: Boolean = false): Boolean

    fun findBoolean(key: String): Boolean?

    fun getInt(key: String, default: Int = 0): Int

    fun findInt(key: String): Int?

    fun getDouble(key: String, default: Double = 0.0): Double

    fun findDouble(key: String): Double?

    fun getFloat(key: String, default: Float = 0f): Float

    fun findFloat(key: String): Float?

    fun getLong(key: String, default: Long = 0L): Long

    fun findLong(key: String): Long?

    fun getStringList(key: String): List<String>

    fun getIntegerList(key: String): List<Int>

    fun getLongList(key: String): List<Long>

    fun getDoubleList(key: String): List<Double>

    fun getFloatList(key: String): List<Float>
}