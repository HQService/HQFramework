package kr.hqservice.framework.nms.virtual.message

import org.bukkit.entity.Player

class VirtualFunc(
    val func: suspend (Player) -> Unit
) {
    suspend fun invoke(player: Player) {
        func(player)
    }
}