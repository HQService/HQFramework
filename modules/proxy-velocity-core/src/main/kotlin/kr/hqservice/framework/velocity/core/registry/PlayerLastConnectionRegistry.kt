package kr.hqservice.framework.velocity.core.registry

import kr.hqservice.framework.global.core.component.Bean
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
class PlayerLastConnectionRegistry {
    private val lastConnectionMap = ConcurrentHashMap<UUID, String>()

    fun restore(playerLastConnectionRegistry: PlayerLastConnectionRegistry) {
        lastConnectionMap.clear()
        lastConnectionMap.putAll(playerLastConnectionRegistry.lastConnectionMap)
    }

    fun findLastConnection(uniqueId: UUID): String? {
        return lastConnectionMap[uniqueId]
    }

    fun setLastConnection(uniqueId: UUID, serverName: String) {
        lastConnectionMap[uniqueId] = serverName
    }
}