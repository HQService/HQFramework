package kr.hqservice.framework.nms.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class AsyncPlayerDataPreLoadEvent(
    val playerId: UUID
) : Event(true) {
    private var kickMessage: String? = null

    fun cancel(message: String) {
        kickMessage = message
    }

    fun getKickMessage(): String? {
        return kickMessage
    }

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