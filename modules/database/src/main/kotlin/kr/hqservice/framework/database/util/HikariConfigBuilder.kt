package kr.hqservice.framework.database.util

import com.zaxxer.hikari.HikariConfig

fun buildHikariConfig(hikariConfigScope: HikariConfig.() -> Unit): HikariConfig {
    return HikariConfig().apply(hikariConfigScope)
}