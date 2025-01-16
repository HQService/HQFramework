package kr.hqservice.framework.nms.v21.virtual.entity

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.world.entity.Entity
import org.bukkit.entity.Player

class VirtualCameraFactoryImpl(
    private val reflectionWrapper: NmsReflectionWrapperImpl
) : VirtualEntityFactory {
    override fun create(player: Player, virtualEntity: AbstractVirtualEntity?): VirtualMessage {
        val stateChangePacket: Any
        val cameraPacket: Any
        if (virtualEntity != null) {
            stateChangePacket = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, 3f)
            cameraPacket = ClientboundSetCameraPacket(virtualEntity.getEntity() as Entity)
        } else {
            val serverPlayer = reflectionWrapper.getEntityPlayer(player)
            stateChangePacket = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode.value.toFloat())
            cameraPacket = ClientboundSetCameraPacket(serverPlayer)
        }

        return VirtualListMessage(listOf(stateChangePacket, cameraPacket))
    }
}