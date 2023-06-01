package kr.hqservice.framework.yaml.extension

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.config.impl.HQYamlConfigurationImpl
import java.io.File

fun File.yaml(): HQYamlConfiguration =
    HQYamlConfigurationImpl().also { it.load(this) }