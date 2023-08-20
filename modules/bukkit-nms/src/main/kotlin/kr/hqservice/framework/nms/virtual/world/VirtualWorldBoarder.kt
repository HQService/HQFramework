package kr.hqservice.framework.nms.virtual.world

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldBoarderWrapper
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class VirtualWorldBoarder(
    world: World
) : Virtual, KoinComponent {
    private val reflectionWrapper: NmsReflectionWrapper by inject()
    private val service: NmsService<World, WorldBoarderWrapper> by inject(named("nms.world.boarder"))
    private val packetClass =
        reflectionWrapper.getNmsClass("ClientboundInitializeBorderPacket", Version.V_15.handle("network.protocol.game"))
    private val boarderWrapper = service.wrap(world)

    fun setCenter(x: Double, z: Double) {
        boarderWrapper.setCenter(x, z)
    }

    fun setSize(size: Int) {
        boarderWrapper.setSize(size)
    }

    override fun createVirtualMessage(): VirtualMessage {
        val constructor = packetClass.java.getConstructor(service.getTargetClass().java)
        val packetInst = constructor.newInstance(boarderWrapper.getUnwrappedInstance())
        return VirtualMessageImpl(packetInst)
    }
}