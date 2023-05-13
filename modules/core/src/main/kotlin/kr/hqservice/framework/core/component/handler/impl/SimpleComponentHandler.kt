package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.component.HQSimpleComponent
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler

@ComponentHandler
class SimpleComponentHandler : HQComponentHandler<HQSimpleComponent> {
    override fun setup(element: HQSimpleComponent) {}

    override fun teardown(element: HQSimpleComponent) {}
}