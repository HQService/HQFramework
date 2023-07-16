package kr.hqservice.framework.view.event

import kr.hqservice.framework.view.HQView
import kr.hqservice.framework.view.element.ButtonElement
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

data class ButtonInteractEvent(
    val view: HQView,
    val buttonElement: ButtonElement,
    private val event: InventoryClickEvent
) {
    fun getSlot(): Int {
        return event.slot
    }

    fun getRawSlot(): Int {
        return event.rawSlot
    }

    fun getClickType(): ClickType {
        return event.click
    }

    fun isShiftClick(): Boolean {
        return event.isShiftClick
    }

    fun isPressedQuickSlotButton(): Boolean {
        return event.hotbarButton >= 0
    }

    fun getQuickSlotButton(): Int {
        return event.hotbarButton
    }

    fun getPlayer(): Player {
        return event.whoClicked as Player
    }
}