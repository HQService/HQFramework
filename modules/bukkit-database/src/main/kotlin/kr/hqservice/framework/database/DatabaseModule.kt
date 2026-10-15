package kr.hqservice.framework.database

import com.zaxxer.hikari.HikariDataSource
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.database.dao.TimestampEntityHooks
import kr.hqservice.framework.database.hook.registry.DatabaseShutdownHookRegistry
import kr.hqservice.framework.database.redis.PubSubTransport
import kr.hqservice.framework.database.redis.RedisProvider
import kr.hqservice.framework.database.repository.player.lifecycle.PlayerDataLifecycle
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.logging.Level
import java.util.logging.Logger

@Module
class DatabaseModule(
    private val database: Database,
    private val databaseShutdownHookRegistry: DatabaseShutdownHookRegistry,
    private val dataSource: HikariDataSource,
    private val playerDataLifecycle: PlayerDataLifecycle,
    private val redisProvider: RedisProvider,
    private val pubSubTransport: PubSubTransport,
    private val logger: Logger
) {
    @Teardown
    fun closeDatabase() {
        try {
            runCatching { playerDataLifecycle.shutdown() }.onFailure {
                logger.log(Level.SEVERE, "failed to release player data ownership on shutdown", it)
            }
            TimestampEntityHooks.unsubscribeAll()
            databaseShutdownHookRegistry.getHooks().forEach { hook ->
                runCatching { hook.shutdown(dataSource) }.onFailure {
                    logger.log(Level.SEVERE, "database shutdown hook ${hook::class.simpleName} failed", it)
                }
            }
            TransactionManager.closeAndUnregister(database)
        } finally {
            try {
                dataSource.close()
            } finally {
                try {
                    pubSubTransport.close()
                } finally {
                    redisProvider.close()
                }
            }
        }
    }
}