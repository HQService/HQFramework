package kr.hqservice.framework.nms.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerDataPreLoadEvent(
    val player: Player
) : Event(true) {
    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}