package kr.hqservice.framework.database

import com.zaxxer.hikari.HikariDataSource
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.database.hook.registry.DatabaseShutdownHookRegistry
import kr.hqservice.framework.database.repository.player.lock.SwitchGate
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.netty.api.NettyServer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

@Module
class DatabaseModule(
    private val nettyServer: NettyServer,
    private val database: Database,
    private val databaseShutdownHookRegistry: DatabaseShutdownHookRegistry,
    private val dataSource: HikariDataSource,
    private val switchGate: SwitchGate
) {
    @Setup
    fun registerPackets() {
        nettyServer.registerInnerPacket(PlayerDataSavedPacket::class) { packet, _ -> }
        nettyServer.registerOuterPacket(PlayerDataSavedPacket::class)
    }

    @Teardown
    fun closeDatabase() {
        TransactionManager.closeAndUnregister(database)
        databaseShutdownHookRegistry.getHooks().forEach {
            it.shutdown(dataSource)
        }
        dataSource.close()
        switchGate.clear()
    }
}