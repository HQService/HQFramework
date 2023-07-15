package kr.hqservice.framework.inventory.event

import kr.hqservice.framework.inventory.button.HQButton
import kr.hqservice.framework.inventory.container.HQContainer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonClickEvent(
    private val container: HQContainer,
    private val button: HQButton,
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

    fun getContainer(): HQContainer {
        return container
    }

    fun getWhoClicked(): Player {
        return event.whoClicked as Player
    }

    fun getButton(): HQButton {
        return button
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