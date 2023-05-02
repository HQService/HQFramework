package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.HQModule
import kr.hqservice.framework.core.component.handler.ComponentHandler
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [ComponentHandler::class])
@Named("module")
class ModuleComponentHandler : ComponentHandler<HQModule> {
    override fun setup(element: HQModule, plugin: HQPlugin) {
        element.onEnable(plugin)
    }

    override fun teardown(element: HQModule, plugin: HQPlugin) {
        element.onDisable(plugin)
    }
}