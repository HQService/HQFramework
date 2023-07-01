package kr.hqservice.framework.database.component.repository

import org.bukkit.entity.Player
import java.util.*

abstract class HQPlayerRepository<V> : HQMemoryRepository<UUID, V>() {
    internal suspend fun onLoad(player: Player) {
        val value = getDataSource().query {
            load(player)
        }
        this[player.uniqueId] = value
    }

    internal suspend fun onSave(player: Player) {
        val value = this[player.uniqueId] ?: return
        getDataSource().query {
            save(player, value)
        }
    }

    abstract suspend fun load(player: Player): V

    abstract suspend fun save(player: Player, value: V)
}