package kr.hqservice.framework.nms.virtual.world

import kr.hqservice.framework.nms.service.world.WorldBorderService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VirtualWorldBorder(
    val world: World
) : Virtual, KoinComponent {
    private val service: WorldBorderService by inject()
    private val borderWrapper = service.wrap(world)
    val packetQueue = mutableMapOf<Int, Any>()

    fun setCenter(x: Double, z: Double) {
        packetQueue[1] = service.createCenterPacket(x, z, borderWrapper)
    }

    fun setWarningDistance(distance: Int) {
        packetQueue[2] = service.createWorldBorderWarningBlocksPacket(distance, borderWrapper)
    }

    fun setSize(size: Double) {
        service.setSize(size, borderWrapper)
    }

    override fun createVirtualMessage(): VirtualMessage =
        service.createVirtualMessage(this, borderWrapper)
}