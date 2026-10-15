package kr.hqservice.framework.database.repository.player.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class PlayerDataSaveFailedEvent(
    val uuid: UUID,
    val playerName: String,
    val consecutiveFailures: Int,
    val cause: Throwable,
    val hint: SaveFailureHint,
) : Event(true) {
    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = getHandlerList()
}
