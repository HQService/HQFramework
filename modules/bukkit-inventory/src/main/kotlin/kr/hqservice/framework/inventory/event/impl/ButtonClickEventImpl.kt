package kr.hqservice.framework.inventory.event.impl

import kr.hqservice.framework.inventory.button.HQButton
import kr.hqservice.framework.inventory.container.HQContainer
import kr.hqservice.framework.inventory.event.ButtonClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

internal class ButtonClickEventImpl(
    private val container: HQContainer,
    private val button: HQButton,
    private val event: InventoryClickEvent
): ButtonClickEvent {
    override fun getSlot(): Int {
        return event.slot
    }

    override fun getRawSlot(): Int {
        return event.rawSlot
    }

    override fun getClickType(): ClickType {
        return event.click
    }

    override fun getContainer(): HQContainer {
        return container
    }

    override fun getWhoClicked(): Player {
        return event.whoClicked as Player
    }

    override fun getButton(): HQButton {
        return button
    }

    override fun isShiftClick(): Boolean {
        return event.isShiftClick
    }

    override fun isPressedQuickSlotButton(): Boolean {
        return event.hotbarButton >= 0
    }

    override fun getQuickSlotButton(): Int {
        return event.hotbarButton
    }
}