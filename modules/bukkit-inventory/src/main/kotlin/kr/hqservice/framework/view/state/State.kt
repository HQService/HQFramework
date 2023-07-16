package kr.hqservice.framework.view.state

interface State<T> {
    fun get(): T

    fun set(value: T)
}