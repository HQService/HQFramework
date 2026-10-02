package kr.hqservice.framework.database.repository.player.lifecycle

import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPickupItemEvent

@Listener
class LoadGuardListener(private val loading: LoadingPlayers) {
    @Subscribe(HandleOrder.FIRST)
    fun pickup(event: PlayerPickupItemEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: InventoryClickEvent) {
        if (loading.contains(event.whoClicked.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerDropItemEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerCommandPreprocessEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerMoveEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }
}
