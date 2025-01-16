package kr.hqservice.framework.nms.virtual.entity

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.entity.factory.VirtualEntityFactory
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

class VirtualSign(
    private val player: Player,
    private val signFactory: VirtualEntityFactory
) : Virtual, KoinComponent {
    override fun createVirtualMessage(): VirtualMessage = signFactory.create(player, null)
}