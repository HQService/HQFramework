package kr.hqservice.framework.nms.wrapper

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Server
import org.bukkit.entity.Player

interface NmsReflectionWrapper {
    fun getEntityPlayer(player: Player): ServerPlayer

    fun getNmsServer(server: Server): DedicatedServer

    suspend fun sendPacket(player: Player, vararg virtual: Virtual)

    suspend fun sendPacket(players: List<Player>, vararg virtual: Virtual)

    suspend fun sendPacket(players: List<Player>, virtualEntity: AbstractVirtualEntity, switchState: Boolean)
}