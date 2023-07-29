package kr.hqservice.framework.bukkit.core.netty.service.impl

import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.global.core.component.Service

@Service
class HQNettyServiceImpl : HQNettyService {
    internal var enabled = false

    override fun isEnable(): Boolean {
        return enabled
    }
}