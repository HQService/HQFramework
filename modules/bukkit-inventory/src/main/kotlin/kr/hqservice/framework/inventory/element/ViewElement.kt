package kr.hqservice.framework.inventory.element

import kr.hqservice.framework.inventory.state.State

sealed interface ViewElement {
    fun subscribe(vararg states: State<*>)
}