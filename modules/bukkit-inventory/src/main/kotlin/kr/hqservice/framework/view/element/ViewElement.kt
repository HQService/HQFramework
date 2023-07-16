package kr.hqservice.framework.view.element

import kr.hqservice.framework.view.state.State

sealed interface ViewElement {
    fun subscribe(vararg states: State<*>)
}