package kr.hqservice.framework.inventory.state.impl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.hqservice.framework.inventory.state.SubscribableState

class MutableStateFlowState<T : Any> internal constructor(value: T) : SubscribableState<T> {
    private val stateFlow = MutableStateFlow(value)

    override fun get(): T {
        return stateFlow.value
    }

    override fun set(value: T) {
        stateFlow.value = value
    }

    override fun getStateFlow(): StateFlow<T> {
        return stateFlow
    }
}