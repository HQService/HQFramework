package kr.hqservice.framework.nms.listener

import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.nms.event.AsyncPlayerDataPreLoadEvent
import kr.hqservice.framework.nms.util.NmsNettyInjectService
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

@Listener
class PlayerConnectionListener(
    private val plugin: Plugin,
    private val injectUtil: NmsNettyInjectService
) {
    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun onJoin(event: PlayerJoinEvent) {
        injectUtil.injectHandler(event.player)
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun onPreJoin(event: AsyncPlayerPreLoginEvent) {
        val loadEvent = AsyncPlayerDataPreLoadEvent(event.uniqueId)
        plugin.server.pluginManager.callEvent(loadEvent)

        val kickMessage = loadEvent.getKickMessage()
        if (kickMessage != null) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = kickMessage
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun onQuit(event: PlayerQuitEvent) {
        injectUtil.removeHandler(event.player)
    }
}