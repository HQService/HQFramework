package kr.hqservice.framework.nms.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class PlayerDataPreLoadEvent(
    val uniqueId: UUID,
) : Event(true) {
    private var valid = true
    private var failReason = StringBuilder()

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()
        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

    fun setFailed(reason: String) {
        valid = false
        failReason.append(reason)
    }

    fun isInvalid(): Boolean {
        return !valid
    }

    fun getFailReason(): String {
        return failReason.toString()
    }
}