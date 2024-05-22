package kr.hqservice.framework.yaml.config.impl

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.config.HQYamlConfigurationSection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

class HQYamlConfigurationImpl : HQYamlConfiguration {
    private var cachedFile: File? = null
    private var loader: YamlConfigurationLoader? = null
    private var rootSection: HQYamlConfigurationSectionImpl? = null

    override fun load(file: File) {
        loader = YamlConfigurationLoader.builder().file(file).build()
        rootSection = HQYamlConfigurationSectionImpl(loader!!.load())
        if (cachedFile == null) cachedFile = file
    }

    override fun save(file: File) {
        val rootSection = rootSection ?: return
        YamlConfigurationLoader
            .builder()
            .nodeStyle(NodeStyle.BLOCK)
            .file(file)
            .build()
            .save(rootSection.getRoot())
    }

    override fun reload() {
        loader = null
        rootSection = null
        cachedFile?.apply(this::load)
    }

    override fun set(key: String, value: Any?) {
        rootSection?.set(key, value)
    }

    override fun getSection(key: String): HQYamlConfigurationSection? {
        return rootSection?.getSection(key)
    }

    override fun getKeys(): List<String> {
        return rootSection?.getKeys() ?: emptyList()
    }

    override fun contains(key: String): Boolean {
        return rootSection?.contains(key) ?: false
    }

    override fun getString(key: String, default: String): String {
        return rootSection?.getString(key) ?: default
    }

    override fun findString(key: String): String? {
        return rootSection?.findString(key)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return rootSection?.getBoolean(key) ?: default
    }

    override fun findBoolean(key: String): Boolean? {
        return rootSection?.findBoolean(key)
    }

    override fun getInt(key: String, default: Int): Int {
        return rootSection?.getInt(key) ?: default
    }

    override fun findInt(key: String): Int? {
        return rootSection?.findInt(key)
    }

    override fun getDouble(key: String, default: Double): Double {
        return rootSection?.getDouble(key) ?: default
    }

    override fun findDouble(key: String): Double? {
        return rootSection?.findDouble(key)
    }

    override fun getFloat(key: String, default: Float): Float {
        return rootSection?.getFloat(key) ?: default
    }

    override fun findFloat(key: String): Float? {
        return rootSection?.findFloat(key)
    }

    override fun getLong(key: String, default: Long): Long {
        return rootSection?.getLong(key) ?: default
    }

    override fun findLong(key: String): Long? {
        return rootSection?.findLong(key)
    }

    override fun getStringList(key: String): List<String> {
        return rootSection?.getStringList(key) ?: emptyList()
    }

    override fun getIntegerList(key: String): List<Int> {
        return rootSection?.getIntegerList(key) ?: emptyList()
    }

    override fun getLongList(key: String): List<Long> {
        return rootSection?.getLongList(key) ?: emptyList()
    }

    override fun getDoubleList(key: String): List<Double> {
        return rootSection?.getDoubleList(key) ?: emptyList()
    }

    override fun getFloatList(key: String): List<Float> {
        return rootSection?.getFloatList(key) ?: emptyList()
    }
}