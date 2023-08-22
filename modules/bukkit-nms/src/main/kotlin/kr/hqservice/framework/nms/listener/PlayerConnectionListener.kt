package kr.hqservice.framework.nms.listener

import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.nms.util.NettyInjectUtil
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Listener
class PlayerConnectionListener(
    private val injectUtil: NettyInjectUtil
) {
    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun onJoin(event: PlayerJoinEvent) {
        injectUtil.injectHandler(event.player)
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun onQuit(event: PlayerQuitEvent) {
        injectUtil.removeHandler(event.player)
    }
}