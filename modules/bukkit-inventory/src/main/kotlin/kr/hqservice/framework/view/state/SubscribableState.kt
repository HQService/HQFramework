package kr.hqservice.framework.view.state

import kotlinx.coroutines.flow.StateFlow

internal interface SubscribableState<T> : State<T> {
    fun getStateFlow(): StateFlow<T>
}