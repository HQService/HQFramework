package kr.hqservice.framework.bukkit.core

import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import org.bukkit.event.player.PlayerJoinEvent

@Listener
class TestListener {
    @Subscribe
    fun playerJoin(event: PlayerJoinEvent) {
        throw IllegalArgumentException("")
    }
}