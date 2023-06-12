package kr.hqservice.framework.bukkit.core.netty.service.impl

import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton

@Component
@HQSingleton(binds = [HQNettyService::class])
class HQNettyServiceImpl : HQNettyService {
    internal var enabled = false

    override fun isEnable(): Boolean {
        return enabled
    }
}