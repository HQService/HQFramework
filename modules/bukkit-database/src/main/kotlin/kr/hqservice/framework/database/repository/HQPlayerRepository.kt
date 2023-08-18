package kr.hqservice.framework.database.repository

import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class HQPlayerRepository<V : Any>(
    dataMap: MutableMap<UUID, V> = ConcurrentHashMap<UUID, V>(),
) : MutableMap<UUID, V> by dataMap, HQComponent {
    abstract suspend fun load(player: Player): V

    abstract suspend fun save(player: Player, value: V)
}