package kr.hqservice.framework.database.repository.player.lifecycle

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerSessionRegistry {
    private val sessions = ConcurrentHashMap<UUID, PlayerSession>()

    fun put(session: PlayerSession) {
        sessions[session.uuid] = session
    }

    fun get(uuid: UUID): PlayerSession? = sessions[uuid]

    fun remove(uuid: UUID): PlayerSession? = sessions.remove(uuid)

    fun all(): List<PlayerSession> = sessions.values.sortedBy { it.uuid }

    fun size(): Int = sessions.size
}
