package kr.hqservice.framework.database.util

import kr.hqservice.framework.database.DatabaseHost
import kr.hqservice.framework.yaml.config.HQYamlConfiguration

fun HQYamlConfiguration.findDatabaseHost(): DatabaseHost? {
    val section = getSection("database") ?: return null
    val host = section.getString("host")
    val port = section.getInt("port")
    val user = section.getString("user")
    val password = section.getString("password")
    val database = section.getString("database")
    return DatabaseHost(host, port, user, password, database)
}

fun HQYamlConfiguration.getDatabaseHost(): DatabaseHost {
    return findDatabaseHost() ?: throw NullPointerException("database config 가 존재하지 않습니다.")
}

fun HQYamlConfiguration.getDatabaseType(): String {
    return getString("database.type")
}

fun HQYamlConfiguration.getDatabasePath(): String {
    return getString("database.path")
}