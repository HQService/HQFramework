package kr.hqservice.framework.view.element

import kr.hqservice.framework.view.state.State
import org.bukkit.entity.Player

class TitleElement(
    private val viewer: Player,
    private val title: String
) : ViewElement {
    internal var subscribedStates: MutableList<State<*>> = mutableListOf()
    internal var titleBuilder: suspend () -> String = { title }

    fun title(titleBuilderScope: suspend () -> String) {
        titleBuilder = titleBuilderScope
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach {
            subscribedStates.add(it)
        }
    }
}