package kr.hqservice.framework.database

data class DatabaseHost(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val database: String
)