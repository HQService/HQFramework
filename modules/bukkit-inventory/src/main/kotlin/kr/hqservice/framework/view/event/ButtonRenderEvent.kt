package kr.hqservice.framework.view.event

import kr.hqservice.framework.view.HQView
import kr.hqservice.framework.view.element.ButtonElement
import org.bukkit.entity.Player

data class ButtonRenderEvent(
    val view: HQView,
    val buttonElement: ButtonElement,
    val player: Player
)