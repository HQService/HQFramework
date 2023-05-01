package kr.hqservice.framework.inventory.event

import kr.hqservice.framework.inventory.button.HQButton
import kr.hqservice.framework.inventory.container.HQContainer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

interface ButtonClickEvent {

    fun getSlot(): Int
    fun getRawSlot(): Int
    fun getClickType(): ClickType
    fun getContainer(): HQContainer
    fun getButton(): HQButton
    fun getWhoClicked(): Player

}