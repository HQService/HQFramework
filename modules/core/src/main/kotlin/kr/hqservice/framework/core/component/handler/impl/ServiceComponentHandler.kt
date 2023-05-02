package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.HQService
import kr.hqservice.framework.core.component.handler.ComponentHandler
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [ComponentHandler::class])
@Named("service")
class ServiceComponentHandler : ComponentHandler<HQService> {
    override fun setup(element: HQService, plugin: HQPlugin) {}

    override fun teardown(element: HQService, plugin: HQPlugin) {}
}