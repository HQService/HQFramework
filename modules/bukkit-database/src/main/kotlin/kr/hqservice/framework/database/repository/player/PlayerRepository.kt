package kr.hqservice.framework.database.repository.player

import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class PlayerRepository<V : Any>(
    private val dataMap: MutableMap<UUID, V> = ConcurrentHashMap<UUID, V>(),
) : MutableMap<UUID, V> by dataMap, HQComponent {
    abstract suspend fun load(player: Player): V

    abstract suspend fun save(player: Player, value: V)

    override fun remove(key: UUID, value: V): Boolean = dataMap.remove(key, value)
}