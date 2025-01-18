package kr.hqservice.framework.nms.virtual.entity

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import org.bukkit.entity.Player

class VirtualSign(
    private val player: Player,
    private val signFactory: VirtualEntityFactory
) : Virtual {
    override fun createVirtualMessage(): VirtualMessage = signFactory.create(player, null)
}