package kr.hqservice.framework.database.repository.player.lifecycle

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class PlayerSession(val uuid: UUID, val player: Player, @Volatile var version: Long) {
    val failures = AtomicInteger()
}
