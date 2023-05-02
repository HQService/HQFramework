package kr.hqservice.framework.core.component.handler

interface ComponentHandler<T> {
    fun setup(element: T)

    fun teardown(element: T)
}