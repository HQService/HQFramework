package kr.hqservice.framework.yaml.config

import java.io.File

interface HQYamlConfiguration : HQYamlConfigurationSection {
    fun load(file: File)

    fun save(file: File)

    fun reload()
}