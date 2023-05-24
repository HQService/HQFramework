package kr.hqservice.framework.global.core.component.handler.impl

import kr.hqservice.framework.global.core.component.HQKoinModule
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.koin.core.component.KoinComponent

@ComponentHandler
class KoinModuleComponentHandler : HQComponentHandler<HQKoinModule>, KoinComponent {
    override fun setup(element: HQKoinModule) {
        getKoin().loadModules(listOf(element.getModule()))
    }

    override fun teardown(element: HQKoinModule) {
        getKoin().unloadModules(listOf(element.getModule()))
    }
}