package kr.hqservice.framework.database.repository.player.lifecycle

import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

@Listener
class LoadGuardListener(private val loading: LoadingPlayers) {
    @Subscribe(HandleOrder.FIRST)
    fun pickup(event: PlayerPickupItemEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onAttemptPickup(event: PlayerAttemptPickupItemEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: InventoryClickEvent) {
        if (loading.contains(event.whoClicked.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        if (loading.contains(player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        if (loading.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onItemHeld(event: PlayerItemHeldEvent) {
        if (loading.contains(event.player.uniqueId))
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
