package kr.hqservice.framework.velocity.core.netty.listener

import com.google.gson.Gson
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent.ServerResult
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import java.io.File
import java.util.*

class PlayerLastConnectionListener(
    plugin: HQVelocityPlugin
) {
    var data: LastServers

    data class LastServers(
        val lastServers: MutableMap<UUID, String>
    )

    private val server = plugin.getProxyServer()

    @Subscribe(order = PostOrder.FIRST)
    fun playerPreLoginEvent(event: ServerPreConnectEvent) {
        if (event.previousServer == null) {
            if (data.lastServers.contains(event.player.uniqueId)) {
                runCatching {
                    event.result = ServerResult.allowed(server.getServer(data.lastServers[event.player.uniqueId]!!).get())
                }
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    fun aaa(event: ServerPreConnectEvent) {
        val result = event.result.server
        if (result.isPresent)
            data.lastServers[event.player.uniqueId] = result.get().serverInfo.name
    }

    init {
        val file = File("last-connection.json")

        data = if (file.exists()) {
            try {
                val lines = file.readLines()
                if (lines.isEmpty()) LastServers(mutableMapOf())
                else Gson().fromJson(file.readLines().joinToString(), LastServers::class.java)
            } catch (_: Exception) {
                LastServers(mutableMapOf())
            }
        } else LastServers(mutableMapOf())
    }
}