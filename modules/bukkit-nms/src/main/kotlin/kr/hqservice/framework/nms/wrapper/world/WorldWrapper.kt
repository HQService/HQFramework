package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.Location

abstract class WorldWrapper : NmsWrapper {
    abstract fun getBlockPosition(location: Location): Any

    abstract fun getIBlockData(blockPosition: Any): Any
}