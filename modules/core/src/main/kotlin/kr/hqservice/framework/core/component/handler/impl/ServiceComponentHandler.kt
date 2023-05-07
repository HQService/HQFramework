package kr.hqservice.framework.core.component.handler.impl

import kr.hqservice.framework.core.component.HQService
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler

@ComponentHandler
class ServiceComponentHandler : HQComponentHandler<HQService> {
    override fun setup(element: HQService) {}

    override fun teardown(element: HQService) {}
}