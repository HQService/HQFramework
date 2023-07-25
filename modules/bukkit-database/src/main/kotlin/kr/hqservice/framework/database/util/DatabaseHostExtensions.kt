package kr.hqservice.framework.database.util

import kr.hqservice.framework.database.DatabaseHost
import kr.hqservice.framework.yaml.config.HQYamlConfigurationSection

fun HQYamlConfigurationSection.findDatabaseHost(sectionKey: String = "database"): DatabaseHost? {
    val section = getSection(sectionKey) ?: return null
    val host = section.getString("host")
    val port = section.getInt("port")
    val user = section.getString("user")
    val password = section.getString("password")
    val database = section.getString("database")
    return DatabaseHost(host, port, user, password, database)
}

fun HQYamlConfigurationSection.getDatabaseHost(sectionKey: String = "database.mysql"): DatabaseHost {
    return findDatabaseHost(sectionKey) ?: throw NullPointerException("database config 가 존재하지 않습니다.")
}

fun HQYamlConfigurationSection.getDatabasePath(sectionKey: String = "database.sqlite"): String {
    return getString("$sectionKey.path")
}

fun HQYamlConfigurationSection.getDatabaseType(sectionKey: String = "database"): String {
    return getString("$sectionKey.type")
}