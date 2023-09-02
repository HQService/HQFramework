package kr.hqservice.framework.view.state.impl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.hqservice.framework.view.state.SubscribableState

class MutableStateFlowState<T> internal constructor(value: T) : SubscribableState<T> {
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