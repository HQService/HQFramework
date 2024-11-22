package kr.hqservice.framework.velocity.core.netty.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent.ServerResult
import com.velocitypowered.api.proxy.ProxyServer
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.velocity.core.registry.PlayerLastConnectionRegistry

@Bean
class PlayerLastConnectionListener(
    private val proxyServer: ProxyServer,
    private val playerLastConnectionRegistry: PlayerLastConnectionRegistry
) {
    @Subscribe(order = PostOrder.FIRST)
    fun onFirstServerPreConnect(event: ServerPreConnectEvent) {
        if (event.previousServer == null) {
            val lastConnection = playerLastConnectionRegistry.findLastConnection(event.player.uniqueId) ?: return
            val optionalRegisteredServer = proxyServer.getServer(lastConnection)
            if (optionalRegisteredServer.isPresent) {
                val registeredServer = optionalRegisteredServer.get()
                event.result = ServerResult.allowed(registeredServer)
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    fun onLastServerPreConnect(event: ServerPreConnectEvent) {
        val result = event.result.server
        if (result.isPresent) {
            playerLastConnectionRegistry.setLastConnection(event.player.uniqueId, result.get().serverInfo.name)
        }
    }
}