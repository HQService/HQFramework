package kr.hqservice.framework.view.event

import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.element.ButtonElement
import org.bukkit.entity.Player

data class ButtonRenderEvent(
    val view: View,
    val buttonElement: ButtonElement,
    val player: Player
)