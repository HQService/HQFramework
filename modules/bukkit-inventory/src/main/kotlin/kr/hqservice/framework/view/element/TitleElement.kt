package kr.hqservice.framework.view.element

import kr.hqservice.framework.view.state.State
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

class TitleElement(
    private val viewer: Player,
    private val title: String
) : ViewElement {
    internal var subscribedStates: MutableList<State<*>> = mutableListOf()
    internal var titleBuilder: suspend () -> BaseComponent = { TextComponent(title) }

    fun title(titleBuilderScope: suspend () -> String) {
        titleBuilder = {
            TextComponent(titleBuilderScope.invoke())
        }
    }

    @JvmName("componentTitle")
    fun title(titleBuilderScope: suspend () -> BaseComponent) {
        titleBuilder = titleBuilderScope
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach {
            subscribedStates.add(it)
        }
    }
}