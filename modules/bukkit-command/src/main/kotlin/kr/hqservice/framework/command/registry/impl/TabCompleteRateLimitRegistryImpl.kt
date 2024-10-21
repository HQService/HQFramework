package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.registry.TabCompleteRateLimitRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
class TabCompleteRateLimitRegistryImpl(
    private val config: HQYamlConfiguration,
) : TabCompleteRateLimitRegistry {
    private val tabCompleteRateLimitRegistry: MutableMap<UUID, Pair<Long, Int>> = ConcurrentHashMap()

    override fun isTabCompletable(playerUniqueId: UUID): Boolean {
        if (!tabCompleteRateLimitRegistry.containsKey(playerUniqueId)) {
            tabCompleteRateLimitRegistry[playerUniqueId] = System.currentTimeMillis() to 1
            return true
        }

        val pair = tabCompleteRateLimitRegistry[playerUniqueId]!!
        if (System.currentTimeMillis() - pair.first > 1000) {
            tabCompleteRateLimitRegistry[playerUniqueId] = System.currentTimeMillis() to 1
            return true
        }
        tabCompleteRateLimitRegistry[playerUniqueId] = pair.first to pair.second + 1
        return pair.second + 1 < getLimitPerSecond()
    }

    private fun getLimitPerSecond(): Int {
        return config.getInt("command.tab-complete.limit-per-second")
    }
}