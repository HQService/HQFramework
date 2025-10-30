package kr.hqservice.framework.database.repository.player.event

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerRepositoryLoadedEvent(
    val player: Player
) : Event(!Bukkit.isPrimaryThread()) {
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