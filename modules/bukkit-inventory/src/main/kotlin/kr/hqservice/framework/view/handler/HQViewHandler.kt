package kr.hqservice.framework.view.handler

import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.view.HQView
import kr.hqservice.framework.view.event.ButtonInteractEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryView

@Component
class HQViewHandler : HQListener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun inventoryClick(event: InventoryClickEvent) {
        getView(event.view)?.apply {
            event.isCancelled = this.cancel
            val button = getButton(event.rawSlot)
            if (button != null) {
                event.isCancelled = true
                button.invokeOnclick(ButtonInteractEvent(this, button, event))
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun inventoryClose(event: InventoryCloseEvent) {
        val view = getView(event.view)
        val player = event.player
        if (view != null && player is Player) {
            view.invokeOnClose(player)
        }
    }

    private fun getView(inventoryView: InventoryView): HQView? {
        val holder = inventoryView.topInventory.holder ?: return null
        return if (holder is HQView) holder else null
    }
}