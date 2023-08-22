package kr.hqservice.framework.inventory.handler

import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.inventory.container.HQContainer
import kr.hqservice.framework.inventory.event.ButtonClickEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.InventoryView

@Listener
class HQContainerHandler {

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClick(event: InventoryClickEvent) {
        getContainer(event.view)?.apply {
            event.isCancelled = isCancelled()

            getButton(event.rawSlot)?.also { button ->
                event.isCancelled = true
                button.click(ButtonClickEvent(this, button, event))
            } ?: onClick(event)
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClose(event: InventoryCloseEvent) {
        getContainer(event.view)?.apply {
            onClose(event)
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryDrag(event: InventoryDragEvent) {
        getContainer(event.view)?.apply {
            onDrag(event)
        }
    }

    private fun getContainer(inventoryView: InventoryView): HQContainer? {
        val holder = inventoryView.topInventory.holder ?: return null
        return if (holder is HQContainer) holder else null
    }
}