package kr.hqservice.framework.velocity.core.netty

import com.google.gson.Gson
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.netty.listener.PlayerConnectionListener
import kr.hqservice.framework.velocity.core.netty.listener.PlayerLastConnectionListener
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.io.File

@Component
class NettyModule(
    config: HQYamlConfiguration,
    private val plugin: HQVelocityPlugin,
    private val bootstrap: NettyServerBootstrap,
    private val channelContainer: NettyChannelRegistry
) : HQModule {
    companion object {
        private var state = false
    }
    private val nettyEnabled: Boolean = config.getBoolean("netty.enabled")
    private val listener = PlayerLastConnectionListener(plugin)

    override fun onEnable() {
        if (state) return
        state = true

        if (nettyEnabled) {
            bootstrap.initializing()
            plugin.getProxyServer().eventManager.register(plugin, PlayerConnectionListener(channelContainer))
            //plugin.getProxyServer().eventManager.register(plugin, listener)
            //plugin.getProxyServer().pluginManager.registerListener(plugin, PlayerConnectionListener(channelContainer))
        }
    }

    override fun onDisable() {
        val file = File("last-connection.json")
        if (!file.exists()) file.createNewFile()

        file.writeText(Gson().toJson(listener.data))

        bootstrap.shutdown()
    }
}