package kr.hqservice.framework.velocity.core.netty

import com.google.gson.Gson
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.module.Module
import kr.hqservice.framework.velocity.core.component.module.Setup
import kr.hqservice.framework.velocity.core.component.module.Teardown
import kr.hqservice.framework.velocity.core.netty.listener.PlayerConnectionListener
import kr.hqservice.framework.velocity.core.netty.listener.PlayerLastConnectionListener
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.io.File

@Module
class NettyModule(
    config: HQYamlConfiguration,
    private val plugin: HQVelocityPlugin,
    private val bootstrap: NettyServerBootstrap,
    private val channelContainer: NettyChannelRegistry
) {
    private val nettyEnabled: Boolean = config.getBoolean("netty.enabled")
    private val listener = PlayerLastConnectionListener(plugin)

    @Setup
    fun setup() {
        if (state) return
        state = true

        if (nettyEnabled) {
            bootstrap.initializing()
            plugin.getProxyServer().eventManager.register(plugin, PlayerConnectionListener(channelContainer))
            //plugin.getProxyServer().eventManager.register(plugin, listener)
            //plugin.getProxyServer().pluginManager.registerListener(plugin, PlayerConnectionListener(channelContainer))
        }
    }

    @Teardown
    fun teardown() {
        val file = File("last-connection.json")
        if (!file.exists()) file.createNewFile()

        file.writeText(Gson().toJson(listener.data))

        bootstrap.shutdown()
    }

    companion object {
        private var state = false
    }
}
