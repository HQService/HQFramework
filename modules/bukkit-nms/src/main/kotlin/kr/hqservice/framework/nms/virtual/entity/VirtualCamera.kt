package kr.hqservice.framework.nms.virtual.entity

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import org.bukkit.entity.Player

class VirtualCamera(
    private val player: Player,
    private val virtualEntity: AbstractVirtualEntity?,
    private val cameraFactory: VirtualEntityFactory
) : Virtual {
    override fun createVirtualMessage(): VirtualMessage = cameraFactory.create(player, virtualEntity)
}