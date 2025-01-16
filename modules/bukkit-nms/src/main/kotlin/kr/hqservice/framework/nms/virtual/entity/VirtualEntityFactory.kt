package kr.hqservice.framework.nms.virtual.entity

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.VirtualMessage
import org.bukkit.entity.Player

interface VirtualEntityFactory {
    fun create(
        player: Player,
        virtualEntity: AbstractVirtualEntity?,
    ): VirtualMessage
}