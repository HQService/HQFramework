package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.registry.TabCompleteRateLimitRegistry
import kr.hqservice.framework.global.core.component.Bean
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
class TabCompleteRateLimitRegistryImpl : TabCompleteRateLimitRegistry {
    private val tabCompleteRateLimitRegistry: MutableMap<UUID, Pair<Long, Int>> = ConcurrentHashMap()

    override fun requestTabComplete(playerUniqueId: UUID): Boolean {
        if(!tabCompleteRateLimitRegistry.containsKey(playerUniqueId)) {
            tabCompleteRateLimitRegistry[playerUniqueId] = System.currentTimeMillis() to 1
            return true
        }

        val pair = tabCompleteRateLimitRegistry[playerUniqueId]!!
        if(System.currentTimeMillis() - pair.first > 1000) {
            tabCompleteRateLimitRegistry[playerUniqueId] = System.currentTimeMillis() to 1
            return true
        }
        tabCompleteRateLimitRegistry[playerUniqueId] = pair.first to pair.second + 1
        return pair.second + 1 < 20
    }

    override fun get(playerUniqueId: UUID): Int {
        return tabCompleteRateLimitRegistry[playerUniqueId]?.second ?: 0
    }
}