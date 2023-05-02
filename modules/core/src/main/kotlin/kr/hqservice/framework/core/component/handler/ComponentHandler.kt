package kr.hqservice.framework.core.component.handler

import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.HQComponent

interface ComponentHandler<T : HQComponent> {
    fun setup(element: T, plugin: HQPlugin)

    fun teardown(element: T, plugin: HQPlugin)
}