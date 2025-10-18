package kr.hqservice.framework.velocity.core.listener

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.module.Module
import kr.hqservice.framework.velocity.core.component.module.Setup
import kotlin.jvm.optionals.getOrNull

@Module
class ServerPingListener(
    private val plugin: HQVelocityPlugin
) {
    private val redis = RedisBungeeAPI.getAbstractRedisBungeeAPI()

    @Setup
    fun setup() {
        plugin.getProxyServer().eventManager.register(plugin, this)
    }

    @Subscribe
    fun onPing(event: ProxyPingEvent) {
        val previous = event.ping
        event.ping = ServerPing(
            previous.version,
            getPlayers(previous.players.getOrNull()),
            previous.descriptionComponent,
            previous.favicon.getOrNull(),
            previous.modinfo.getOrNull()
        )
    }

    private fun getPlayers(previous: ServerPing.Players?): ServerPing.Players {
        return ServerPing.Players(
            redis.playersOnline.size,
            previous?.max ?: 10000,
            emptyList()
        )
    }
}