package kr.hqservice.framework.nms.listener

import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.nms.util.NettyInjectUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Component
class PlayerConnectionListener(
    private val injectUtil: NettyInjectUtil
) : HQListener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        val channel = injectUtil.getPlayerChannel(event.player)
        injectUtil.injectHandler(event.player, channel)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(event: PlayerQuitEvent) {
        injectUtil.removeHandler(injectUtil.getPlayerChannel(event.player))
    }
}