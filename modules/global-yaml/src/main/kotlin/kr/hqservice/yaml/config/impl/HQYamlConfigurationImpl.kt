package kr.hqservice.yaml.config.impl

import kr.hqservice.yaml.config.HQYamlConfiguration
import kr.hqservice.yaml.config.HQYamlConfigurationSection
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import java.io.File

class HQYamlConfigurationImpl : HQYamlConfiguration {
    private var cachedFile: File? = null
    private var rootSection: HQYamlConfigurationSectionImpl? = null

    override fun load(file: File) {
        rootSection = HQYamlConfigurationSectionImpl(YAMLConfigurationLoader.builder().setFile(file).build().load())
        cachedFile = file
    }

    override fun reload() {
        rootSection = null
        cachedFile?.apply(this::load)
    }

    override fun getSection(key: String): HQYamlConfigurationSection? {
        return rootSection?.getSection(key)
    }

    override fun getString(key: String): String {
        return rootSection?.getString(key) ?: ""
    }

    override fun getBoolean(key: String): Boolean {
        return rootSection?.getBoolean(key) ?: false
    }

    override fun getInt(key: String): Int {
        return rootSection?.getInt(key)?: 0
    }

    override fun getDouble(key: String): Double {
        return rootSection?.getDouble(key)?: .0
    }

    override fun getLong(key: String): Long {
        return rootSection?.getLong(key)?: 0
    }
}