package kr.hqservice.framework.nms.listener

import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.nms.util.NettyInjectUtil
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

@Listener
class PlayerConnectionListener(
    private val injectUtil: NettyInjectUtil
) {
    @Subscribe(HandleOrder.FIRST)
    fun onJoinFirst(event: AsyncPlayerPreLoginEvent) {
        injectUtil.injectHandler(event.uniqueId, event.address.hostAddress)
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun onQuit(event: PlayerQuitEvent) {
        injectUtil.removeHandler(event.player)
    }
}