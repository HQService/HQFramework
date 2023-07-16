package kr.hqservice.framework.inventory.state

import kotlinx.coroutines.flow.StateFlow

internal interface SubscribableState<T> : State<T> {
    fun getStateFlow(): StateFlow<T>
}