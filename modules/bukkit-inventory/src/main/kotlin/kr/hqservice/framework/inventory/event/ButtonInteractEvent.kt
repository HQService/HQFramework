package kr.hqservice.framework.inventory.event

import kr.hqservice.framework.inventory.HQView
import kr.hqservice.framework.inventory.element.ButtonElement
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonInteractEvent(
    private val view: HQView,
    private val buttonElement: ButtonElement,
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

    fun getView(): HQView {
        return view
    }

    fun getWhoClicked(): Player {
        return event.whoClicked as Player
    }

    fun getButtonElement(): ButtonElement {
        return buttonElement
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
}