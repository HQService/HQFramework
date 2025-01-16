package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.Location

interface WorldWrapper : NmsWrapper {
    fun getBlockPosition(location: Location): Any

    fun getIBlockData(blockPosition: Any): Any
}