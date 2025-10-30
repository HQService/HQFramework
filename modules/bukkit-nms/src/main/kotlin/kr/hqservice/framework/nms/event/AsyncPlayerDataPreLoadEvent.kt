package kr.hqservice.framework.nms.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AsyncPlayerDataPreLoadEvent(
    val player: Player
) : Event(true) {
    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()
        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }
}