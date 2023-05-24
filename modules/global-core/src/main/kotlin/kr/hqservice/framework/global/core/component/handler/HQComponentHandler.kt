package kr.hqservice.framework.global.core.component.handler

import kr.hqservice.framework.global.core.component.HQComponent

interface HQComponentHandler<T : HQComponent> {
    fun setup(element: T) {}

    fun teardown(element: T) {}
}