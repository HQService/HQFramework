package kr.hqservice.framework.region.event.impl

import kr.hqservice.framework.region.data.Region
import kr.hqservice.framework.region.event.RegionEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class RegionLeaveEvent(
    val player: Player,
    val moveType: MoveType,
    val fromRegion: Region,
    val toRegion: Region?
) : RegionEvent() {

    enum class MoveType {
        TELEPORT,
        MOVE,
        DEATH,
        QUIT
    }

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }
}