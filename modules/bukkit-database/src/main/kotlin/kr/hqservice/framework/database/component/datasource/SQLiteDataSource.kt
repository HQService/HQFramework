package kr.hqservice.framework.database.component.datasource

import com.zaxxer.hikari.HikariConfig
import kr.hqservice.framework.database.util.buildHikariConfig
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException

abstract class SQLiteDataSource(
    private val databasePath: String,
    private val poolName: String = "unnamed SQLiteDataSource pool"
) : HQDataSource() {
    override fun setupDatabase() {
        val databaseFolder = File(databasePath.split("/").toMutableList().apply { removeLast() }.joinToString("/"))
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs()
        }
        val databaseFile = File(databasePath)
        try {
            databaseFile.createNewFile()
        } catch (e: IOException) {
            throw IOException("SQLite DataSource 파일을 생성하는 것을 실패하였습니다. 직접 ${databasePath} 경로에 파일을 생성하여주세요.", e)
        }
        databaseLazy.value
    }

    override fun getConfig(): HikariConfig {
        return buildHikariConfig {
            this.jdbcUrl = "jdbc:sqlite:$databasePath"
            this.connectionTestQuery = "SELECT 1"
            this.poolName = this@SQLiteDataSource.poolName
        }
    }
}