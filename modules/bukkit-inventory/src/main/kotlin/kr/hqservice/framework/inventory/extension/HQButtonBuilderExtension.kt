package kr.hqservice.framework.inventory.extension

import kr.hqservice.framework.inventory.button.HQButtonBuilder
import kr.hqservice.framework.inventory.event.ButtonClickEvent
import org.bukkit.event.inventory.ClickType

fun HQButtonBuilder.setLeftClickFunction(onClick: (ButtonClickEvent) -> Unit): HQButtonBuilder {
    return setClickFunction { event ->
        if (event.getClickType() != ClickType.LEFT) return@setClickFunction
        onClick(event)
    }
}