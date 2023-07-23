package kr.hqservice.framework.database.component.repository

import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class HQPlayerRepository<V>(
    dataMap: MutableMap<UUID, V> = ConcurrentHashMap<UUID, V>()
) : HQRepository, MutableMap<UUID, V> by dataMap {
    internal suspend fun onLoad(player: Player) {
        val value = dataSource.query {
            load(player)
        }
        this[player.uniqueId] = value
    }

    internal suspend fun onSave(player: Player) {
        val value = this[player.uniqueId] ?: return
        dataSource.query {
            save(player, value)
        }
    }

    abstract suspend fun load(player: Player): V

    abstract suspend fun save(player: Player, value: V)
}