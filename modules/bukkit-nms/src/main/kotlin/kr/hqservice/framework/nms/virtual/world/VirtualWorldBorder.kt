package kr.hqservice.framework.nms.virtual.world

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class VirtualWorldBorder(
    world: World
) : Virtual, KoinComponent {
    private val service: NmsService<World, WorldBorderWrapper> by inject(named("nms.world.border"))
    private val borderWrapper = service.wrap(world)
    private var packetQueue = mutableMapOf<Int, Packet<*>>()

    fun setCenter(x: Double, z: Double) {
        packetQueue[1] = WorldBorderCenter(x, z).createPacket()
    }

    fun setWarningDistance(distance: Int) {
        packetQueue[2] = WorldBorderWarningBlocks(distance).createPacket()
    }

    fun setSize(size: Double) {
        borderWrapper.getUnwrappedInstance().size = size
    }

    override fun createVirtualMessage(): VirtualMessage {
        val packetInst = ClientboundInitializeBorderPacket(borderWrapper.getUnwrappedInstance())
        return if (packetQueue.isNotEmpty()) {
            val result = VirtualListMessage(listOf(packetInst) + packetQueue.values)
            packetQueue.clear()
            result
        } else VirtualMessageImpl(packetInst)
    }

    private inner class WorldBorderCenter(
        private val x: Double,
        private val z: Double
    ) {
        fun createPacket(): Packet<*> {
            borderWrapper.getUnwrappedInstance().setCenter(x, z)
            return ClientboundSetBorderCenterPacket(borderWrapper.getUnwrappedInstance())
        }
    }

    private inner class WorldBorderWarningBlocks(
        private val blocks: Int
    ) {
        fun createPacket(): Packet<*> {
            borderWrapper.getUnwrappedInstance().warningBlocks = blocks
            return ClientboundSetBorderWarningDistancePacket(borderWrapper.getUnwrappedInstance())
        }
    }
}