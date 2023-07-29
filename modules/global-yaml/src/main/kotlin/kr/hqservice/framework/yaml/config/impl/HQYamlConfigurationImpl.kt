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

    override fun getSection(key: String): HQYamlConfigurationSection? {
        return rootSection?.getSection(key)
    }

    override fun getString(key: String): String {
        return rootSection?.getString(key) ?: ""
    }

    override fun getKeys(): List<String> {
        return rootSection?.getKeys() ?: emptyList()
    }

    override fun getStringList(key: String): List<String> {
        return rootSection?.getStringList(key) ?: emptyList()
    }

    override fun getIntegerList(key: String): List<Int> {
        return rootSection?.getIntegerList(key) ?: emptyList()
    }

    override fun getBoolean(key: String): Boolean {
        return rootSection?.getBoolean(key) ?: false
    }

    override fun getInt(key: String): Int {
        return rootSection?.getInt(key) ?: 0
    }

    override fun getDouble(key: String): Double {
        return rootSection?.getDouble(key) ?: .0
    }

    override fun getLong(key: String): Long {
        return rootSection?.getLong(key) ?: 0
    }

    override fun set(key: String, value: Any?) {
        rootSection?.set(key, value)
    }
}