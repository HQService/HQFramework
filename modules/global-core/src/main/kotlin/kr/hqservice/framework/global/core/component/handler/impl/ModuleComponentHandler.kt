package kr.hqservice.framework.global.core.component.handler.impl

import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.extension.print

@ComponentHandler
class ModuleComponentHandler : HQComponentHandler<HQModule> {
    override fun setup(element: HQModule) {
        element.onEnable()
    }

    override fun teardown(element: HQModule) {
        element.onDisable()
    }
}