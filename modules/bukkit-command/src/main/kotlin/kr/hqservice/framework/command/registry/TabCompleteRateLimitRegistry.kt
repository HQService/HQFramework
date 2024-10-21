package kr.hqservice.framework.command.registry

import java.util.*

interface TabCompleteRateLimitRegistry {
    fun isTabCompletable(playerUniqueId: UUID): Boolean
}