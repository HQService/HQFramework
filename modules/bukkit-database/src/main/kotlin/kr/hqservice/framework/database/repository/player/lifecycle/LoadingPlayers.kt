package kr.hqservice.framework.database.repository.player.lifecycle

import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Bean
class LoadingPlayers {
    private val ids = ConcurrentHashMap.newKeySet<UUID>()

    fun add(uuid: UUID) {
        ids.add(uuid)
    }

    fun remove(uuid: UUID) {
        ids.remove(uuid)
    }

    operator fun contains(uuid: UUID): Boolean = uuid in ids
}
