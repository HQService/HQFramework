package kr.hqservice.yaml.config

import java.io.File

interface HQYamlConfiguration : HQYamlConfigurationSection {
    fun load(file: File)

    fun reload()
}