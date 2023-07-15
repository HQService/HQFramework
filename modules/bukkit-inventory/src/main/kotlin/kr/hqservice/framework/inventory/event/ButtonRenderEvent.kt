package kr.hqservice.framework.inventory.event

import kr.hqservice.framework.inventory.HQView
import kr.hqservice.framework.inventory.element.ButtonElement
import org.bukkit.entity.Player

class ButtonRenderEvent(
    private val view: HQView,
    private val buttonElement: ButtonElement,
    private val player: Player
) : ButtonEvent {
    override fun getView(): HQView {
        return view
    }

    override fun getButtonElement(): ButtonElement {
        return buttonElement
    }

    override fun getPlayer(): Player {
        return player
    }
}