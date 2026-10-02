package kr.hqservice.framework.database

import com.zaxxer.hikari.HikariDataSource
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.database.dao.TimestampEntityHooks
import kr.hqservice.framework.database.hook.registry.DatabaseShutdownHookRegistry
import kr.hqservice.framework.database.repository.player.lock.SwitchGate
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavePacket
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.netty.api.NettyServer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.logging.Level
import java.util.logging.Logger

@Module
class DatabaseModule(
    private val nettyServer: NettyServer,
    private val database: Database,
    private val databaseShutdownHookRegistry: DatabaseShutdownHookRegistry,
    private val dataSource: HikariDataSource,
    private val switchGate: SwitchGate,
    private val logger: Logger
) {
    @Setup
    fun registerPackets() {
        nettyServer.registerInnerPacket(PlayerDataSavedPacket::class) { packet, _ -> }
        nettyServer.registerOuterPacket(PlayerDataSavedPacket::class)

        nettyServer.registerInnerPacket(PlayerDataSavePacket::class) { packet, _ -> }
        nettyServer.registerOuterPacket(PlayerDataSavePacket::class)
    }

    @Teardown
    fun closeDatabase() {
        try {
            TimestampEntityHooks.unsubscribeAll()
            databaseShutdownHookRegistry.getHooks().forEach { hook ->
                runCatching { hook.shutdown(dataSource) }.onFailure {
                    logger.log(Level.SEVERE, "database shutdown hook ${hook::class.simpleName} failed", it)
                }
            }
            TransactionManager.closeAndUnregister(database)
        } finally {
            dataSource.close()
            switchGate.clear()
        }
    }
}