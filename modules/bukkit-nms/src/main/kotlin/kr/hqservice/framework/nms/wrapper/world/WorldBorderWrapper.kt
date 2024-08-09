package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import net.minecraft.world.level.border.WorldBorder

class WorldBorderWrapper(
    private val worldBorder: WorldBorder,
) : NmsWrapper {
    override fun getUnwrappedInstance(): WorldBorder {
        return worldBorder
    }
}