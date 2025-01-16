package kr.hqservice.framework.nms.wrapper

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import org.bukkit.entity.Player

interface NmsReflectionWrapper {
    suspend fun sendPacket(player: Player, vararg virtual: Virtual)

    suspend fun sendPacket(players: List<Player>, vararg virtual: Virtual)

    suspend fun sendPacket(players: List<Player>, virtualEntity: AbstractVirtualEntity, switchState: Boolean)
}