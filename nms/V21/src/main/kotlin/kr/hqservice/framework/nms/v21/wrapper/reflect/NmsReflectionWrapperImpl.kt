package kr.hqservice.framework.nms.v21.wrapper.reflect

import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.minecraft.network.protocol.Packet
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Server
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

class NmsReflectionWrapperImpl : NmsReflectionWrapper, HQSimpleComponent {
    fun getEntityPlayer(player: Player): ServerPlayer {
        return (player as CraftPlayer).handle
    }

    fun getNmsServer(server: Server): DedicatedServer {
        return (server as CraftServer).server
    }

    override suspend fun sendPacket(player: Player, vararg virtual: Virtual) {
        val handle = (player as CraftPlayer).handle
        val connection = handle.connection
        virtual.forEach {
            it.createVirtualMessage()?.also { virtual ->
                virtual.send { packet ->
                    connection.sendPacket(packet as Packet<*>)
                    if (it is VirtualContainer) player.updateInventory()
                }
            }
        }
    }

    override suspend fun sendPacket(players: List<Player>, vararg virtual: Virtual) {
        virtual.forEach {
            it.createVirtualMessage()?.also { virtual ->
                players.forEach { player ->
                    val handle = (player as CraftPlayer).handle
                    val connection = handle.connection
                    virtual.send { packet ->
                        connection.sendPacket(packet as Packet<*>)
                    }
                }
            }
        }
    }

    override suspend fun sendPacket(players: List<Player>, virtualEntity: AbstractVirtualEntity, switchState: Boolean) {
        virtualEntity.createVirtualMessage(switchState)?.also { virtual ->
            players.forEach { player ->
                val handle = (player as CraftPlayer).handle
                val connection = handle.connection
                virtual.send { packet ->
                    connection.sendPacket(packet as Packet<*>)
                }
            }
        }
    }
}