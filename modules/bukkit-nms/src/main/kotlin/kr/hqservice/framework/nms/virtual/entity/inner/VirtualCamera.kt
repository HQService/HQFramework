package kr.hqservice.framework.nms.virtual.entity.inner

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import org.bukkit.entity.Player

class VirtualCamera(
    private val player: Player,
    private val virtualEntity: AbstractVirtualEntity?,
    private val reflectionWrapper: NmsReflectionWrapper
) : Virtual {
    override fun createVirtualMessage(): VirtualMessage {
        val stateChangePacket: Any
        val cameraPacket: Any
        if (virtualEntity != null) {
            stateChangePacket = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, 3f)
            cameraPacket = ClientboundSetCameraPacket(virtualEntity.getEntity())
        } else {
            val serverPlayer = reflectionWrapper.getEntityPlayer(player)
            stateChangePacket = ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode.value.toFloat())
            cameraPacket = ClientboundSetCameraPacket(serverPlayer)
        }

        return VirtualListMessage(listOf(stateChangePacket, cameraPacket))
    }
}