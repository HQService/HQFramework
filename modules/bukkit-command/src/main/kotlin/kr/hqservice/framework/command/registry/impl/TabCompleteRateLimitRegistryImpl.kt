package kr.hqservice.framework.command.registry.impl

import kr.hqservice.framework.command.registry.TabCompleteRateLimitRegistry
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
class TabCompleteRateLimitRegistryImpl(
    private val config: HQYamlConfiguration,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : TabCompleteRateLimitRegistry {

    private val tabCompleteRateLimitRegistry: ConcurrentHashMap<UUID, Pair<Long, Int>> = ConcurrentHashMap()

    override fun isTabCompletable(playerUniqueId: UUID): Boolean {
        val now = nowMillis()
        tabCompleteRateLimitRegistry.values.removeIf { now - it.first > 1000 }
        val (_, count) = tabCompleteRateLimitRegistry.compute(playerUniqueId) { _, window ->
            if (window == null || now - window.first > 1000) now to 1 else window.first to window.second + 1
        }!!
        return count <= getLimitPerSecond()
    }

    private fun getLimitPerSecond() = config.getInt("command.tab-complete.limit-per-second", 20)

}
