package kr.hqservice.framework.view.element

import kr.hqservice.framework.view.state.State
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent

class TitleElement: ViewElement {
    internal var subscribedStates: MutableList<State<*>> = mutableListOf()
    internal var titleBuilder: suspend () -> BaseComponent? = { null }

    fun text(titleBuilderScope: suspend () -> String?) {
        titleBuilder = {
            titleBuilderScope.invoke()?.let { TextComponent(it) }
        }
    }

    fun component(titleBuilderScope: suspend () -> BaseComponent?) {
        titleBuilder = titleBuilderScope
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach {
            subscribedStates.add(it)
        }
    }
}