package kr.hqservice.framework.inventory.event

import kr.hqservice.framework.inventory.HQView
import kr.hqservice.framework.inventory.element.ButtonElement
import org.bukkit.entity.Player

sealed interface ButtonEvent {
    fun getView(): HQView

    fun getButtonElement(): ButtonElement

    fun getPlayer(): Player
}