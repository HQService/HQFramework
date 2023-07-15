package kr.hqservice.framework.inventory.state

import kotlinx.coroutines.flow.StateFlow

interface State<T> {
    fun get(): T

    fun set(value: T)

    fun getStateFlow(): StateFlow<T>
}