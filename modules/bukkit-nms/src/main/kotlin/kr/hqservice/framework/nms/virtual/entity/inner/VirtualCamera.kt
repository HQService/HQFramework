package kr.hqservice.framework.nms.virtual.entity.inner

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VirtualCamera(
    private val player: Player,
    private val virtualEntity: AbstractVirtualEntity?
) : Virtual, KoinComponent {
    private val reflectionWrapper: NmsReflectionWrapper by inject()
    private val entityClass = reflectionWrapper.getNmsClass("Entity", Version.V_15.handle("world.entity"))
    private val stateChangePacketClass = reflectionWrapper.getNmsClass("PacketPlayOutGameStateChange", Version.V_15.handle("network.protocol.game"))
    private val changeGameModeType = reflectionWrapper.getStaticField(stateChangePacketClass, "d").call()!!
    private val cameraPacketClass = reflectionWrapper.getNmsClass("PacketPlayOutCamera", Version.V_15.handle("network.protocol.game"))

    override fun createVirtualMessage(): VirtualMessage {
        val stateChangePacket: Any
        val cameraPacket: Any
        if(virtualEntity != null) {
            stateChangePacket =
                stateChangePacketClass.java.getConstructor(changeGameModeType::class.java, Float::class.java)
                    .newInstance(changeGameModeType, 3f)
            cameraPacket = cameraPacketClass.java.getConstructor(entityClass.java)
                .newInstance(virtualEntity.getEntity())
        } else {
            val serverPlayer = reflectionWrapper.getEntityPlayer(player)
            stateChangePacket =
                stateChangePacketClass.java.getConstructor(changeGameModeType::class.java, Float::class.java)
                    .newInstance(changeGameModeType, player.gameMode.value.toFloat())
            cameraPacket = cameraPacketClass.java.getConstructor(entityClass.java)
                .newInstance(serverPlayer)
        }
        return VirtualListMessage(listOf(stateChangePacket, cameraPacket))
    }
}