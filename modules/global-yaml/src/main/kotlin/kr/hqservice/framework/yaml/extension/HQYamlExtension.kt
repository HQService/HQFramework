package kr.hqservice.framework.yaml.extension

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.config.impl.HQYamlConfigurationImpl
import java.io.File
import java.util.UUID

fun File.yaml(): HQYamlConfiguration =
    HQYamlConfigurationImpl().also { it.load(this) }

fun HQYamlConfiguration.findUUID(path: String): UUID? {
    return runCatching { getString(path).run(UUID::fromString) }.getOrNull()
}

fun HQYamlConfiguration.getUUID(path: String): UUID? {
    val uniqueIdText = getString(path)
    return runCatching {
        UUID.fromString(uniqueIdText)
    }.getOrNull() ?: throw NullPointerException("${uniqueIdText}은(는) 존재하지 않는 UUID입니다.")
}

fun HQYamlConfiguration.getUUIDList(path: String): List<UUID> {
    return getStringList(path).map(UUID::fromString)
}