package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import org.bukkit.World

interface NmsWorldBorderService : NmsService<World, WorldBorderWrapper> {
    fun createCenterPacket(x: Double, z: Double, wrapper: WorldBorderWrapper): Any

    fun createWorldBorderWarningBlocksPacket(distance: Int, wrapper: WorldBorderWrapper): Any

    fun setSize(size: Double, wrapper: WorldBorderWrapper)

    fun createVirtualMessage(border: VirtualWorldBorder, wrapper: WorldBorderWrapper): VirtualMessage
}