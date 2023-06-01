package kr.hqservice.framework.inventory.handler

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.inventory.container.HQContainer
import kr.hqservice.framework.inventory.event.impl.ButtonClickEventImpl
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.InventoryView

@Component
class HQContainerHandler: HQListener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun inventoryClick(event: InventoryClickEvent) {
        getContainer(event.view)?.apply {
            event.isCancelled = isCancelled()

            getButton(event.rawSlot)?.also { button ->
                event.isCancelled = true
                button.click(ButtonClickEventImpl(this, button, event))
            }?: onClick(event)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun inventoryClose(event: InventoryCloseEvent) {
        getContainer(event.view)?.apply {
            onClose(event)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun inventoryDrag(event: InventoryDragEvent) {
        getContainer(event.view)?.apply {
            onDrag(event)
        }
    }

    private fun getContainer(inventoryView: InventoryView): HQContainer? {
        val holder = inventoryView.topInventory.holder?: return null
        return if(holder is HQContainer) holder else null
    }

}