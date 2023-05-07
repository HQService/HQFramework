package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.component.HQModule
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler

@ComponentHandler
class ModuleComponentHandler : HQComponentHandler<HQModule> {
    override fun setup(element: HQModule) {
        element.onEnable()
    }

    override fun teardown(element: HQModule) {
        element.onDisable()
    }
}