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
    @Subscribe(handleOrder = HandleOrder.EARLY)
    fun inventoryClick(event: InventoryClickEvent) {
        getContainer(event.view)?.apply {
            event.isCancelled = isCancelled()
            val button = getButton(event.rawSlot)
            if (button != null) {
                if (event.currentItem?.run { if (type.isAir) null else this } == null) onClick(event)
                else {
                    event.isCancelled = true
                    button.click(ButtonClickEvent(this, button, event))
                }
            } else onClick(event)
        }
    }

    @Subscribe(handleOrder = HandleOrder.EARLY)
    fun inventoryClose(event: InventoryCloseEvent) {
        getContainer(event.view)?.apply {
            onClose(event)
        }
    }

    @Subscribe(handleOrder = HandleOrder.EARLY)
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