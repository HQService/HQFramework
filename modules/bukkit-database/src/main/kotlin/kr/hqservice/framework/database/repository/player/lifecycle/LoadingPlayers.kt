package kr.hqservice.framework.database.repository.player.lifecycle

import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Bean
class LoadingPlayers {
    private val tokens = ConcurrentHashMap<UUID, Long>()
    private val nextToken = AtomicLong()

    fun begin(uuid: UUID): Long = nextToken.incrementAndGet().also { tokens[uuid] = it }

    fun end(uuid: UUID, token: Long) {
        tokens.remove(uuid, token)
    }

    fun remove(uuid: UUID) {
        tokens.remove(uuid)
    }

    fun isCurrent(uuid: UUID, token: Long): Boolean = tokens[uuid] == token

    fun isSuperseded(uuid: UUID, token: Long): Boolean = tokens[uuid].let { it != null && it != token }

    operator fun contains(uuid: UUID): Boolean = tokens.containsKey(uuid)
}
