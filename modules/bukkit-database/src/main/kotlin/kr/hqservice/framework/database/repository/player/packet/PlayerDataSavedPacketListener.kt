package kr.hqservice.framework.database.repository.player.packet

import kr.hqservice.framework.database.repository.player.lifecycle.PlayerDataLifecycle
import kr.hqservice.framework.netty.packet.listener.PacketListener
import kr.hqservice.framework.netty.packet.listener.PacketSubscribe

@PacketListener
class PlayerDataSavedPacketListener(private val lifecycle: PlayerDataLifecycle) {
    @PacketSubscribe
    fun onSaved(packet: PlayerDataSavedPacket) {
        lifecycle.onPeerSaved(packet)
    }
}
