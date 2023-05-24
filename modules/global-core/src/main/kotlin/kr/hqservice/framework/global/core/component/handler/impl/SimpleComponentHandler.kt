package kr.hqservice.framework.global.core.component.handler.impl

import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler

@ComponentHandler
class SimpleComponentHandler : HQComponentHandler<HQSimpleComponent> {
    override fun setup(element: HQSimpleComponent) {}

    override fun teardown(element: HQSimpleComponent) {}
}