package kr.hqservice.framework.core.component.handler

import kr.hqservice.framework.core.component.HQComponent

interface HQComponentHandler<T : HQComponent> {
    fun setup(element: T)

    fun teardown(element: T)
}