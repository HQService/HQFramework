package kr.hqservice.framework.bukkit.core.netty.service

import kr.hqservice.framework.global.core.component.HQService

interface HQNettyService : HQService {
    fun isEnable(): Boolean
}