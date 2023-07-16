package kr.hqservice.framework.inventory.state

interface State<T> {
    fun get(): T

    fun set(value: T)
}