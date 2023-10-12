package kr.hqservice.framework.command.registry

import java.util.*

interface TabCompleteRateLimitRegistry {
    fun requestTabComplete(playerUniqueId: UUID): Boolean

    fun get(playerUniqueId: UUID): Int
}