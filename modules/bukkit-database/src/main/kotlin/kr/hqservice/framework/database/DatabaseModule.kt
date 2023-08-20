package kr.hqservice.framework.database

import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.component.Module
import kr.hqservice.framework.netty.api.NettyServer
import org.jetbrains.exposed.sql.transactions.TransactionManager

@Module
class DatabaseModule(
    private val nettyServer: NettyServer,
) : HQModule {
    override fun onEnable() {
        nettyServer.registerInnerPacket(PlayerDataSavedPacket::class) { packet, _ -> }
        nettyServer.registerOuterPacket(PlayerDataSavedPacket::class)
    }

    override fun onDisable() {
        val database = TransactionManager.defaultDatabase
        if (database != null) {
            TransactionManager.closeAndUnregister(database)
        }
    }
}