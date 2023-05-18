package kr.hqservice.yaml.extension

import kr.hqservice.yaml.config.HQYamlConfiguration
import kr.hqservice.yaml.config.impl.HQYamlConfigurationImpl
import java.io.File

fun File.yaml(): HQYamlConfiguration =
    HQYamlConfigurationImpl().also { it.load(this) }