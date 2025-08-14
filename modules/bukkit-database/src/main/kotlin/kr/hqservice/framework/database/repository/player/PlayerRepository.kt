package kr.hqservice.framework.database.repository.player

import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class PlayerRepository<V : Any>(
    dataMap: MutableMap<UUID, V> = ConcurrentHashMap<UUID, V>(),
) : MutableMap<UUID, V> by dataMap, HQComponent {
    private val preLoadData = ConcurrentHashMap<UUID, V>()

    abstract suspend fun load(player: Player): V

    abstract suspend fun save(player: Player, value: V)

    internal suspend fun preLoad0(uniqueId: UUID) {
        preLoadData.remove(uniqueId)
        preLoad(uniqueId)?.let { preLoadData[uniqueId] = it }
    }

    internal fun preValue(player: Player): V? {
        return preLoadData.remove(player.uniqueId)
    }

    internal fun removePreLoad(uniqueId: UUID) {
        preLoadData.remove(uniqueId)
    }

    open suspend fun preLoad(uniqueId: UUID): V? = null
}