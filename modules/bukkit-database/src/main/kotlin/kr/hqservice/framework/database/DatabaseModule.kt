package kr.hqservice.framework.database

import kr.hqservice.framework.database.packet.PlayerDataSavedPacket
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.netty.api.NettyServer

@Component
class DatabaseModule(private val nettyServer: NettyServer) : HQModule {
    override fun onEnable() {
        nettyServer.registerInnerPacket(PlayerDataSavedPacket::class) { packet, _ ->
            packet.print("packet received")
        }
        nettyServer.registerOuterPacket(PlayerDataSavedPacket::class)
    }
}