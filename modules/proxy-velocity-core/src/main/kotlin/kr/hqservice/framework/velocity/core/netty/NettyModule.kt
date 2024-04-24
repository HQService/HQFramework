package kr.hqservice.framework.velocity.core.netty

import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.netty.listener.PlayerConnectionListener
import kr.hqservice.framework.yaml.config.HQYamlConfiguration

@Component
class NettyModule(
    config: HQYamlConfiguration,
    private val plugin: HQVelocityPlugin,
    private val bootstrap: NettyServerBootstrap,
    private val channelContainer: NettyChannelRegistry
) : HQModule {
    private val nettyEnabled: Boolean = config.getBoolean("netty.enabled")

    override fun onEnable() {
        if (nettyEnabled) {
            bootstrap.initializing()
            plugin.getProxyServer().eventManager.register(plugin, PlayerConnectionListener(channelContainer))
            //plugin.getProxyServer().pluginManager.registerListener(plugin, PlayerConnectionListener(channelContainer))
        }
    }

    override fun onDisable() {
        bootstrap.shutdown()
    }
}