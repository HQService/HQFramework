package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.bungee.core.netty.listener.PlayerConnectionListener
import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

@Component
class NettyModule(
    config: HQYamlConfiguration,
    private val plugin: Plugin,
    private val bootstrap: NettyServerBootstrap,
    private val channelContainer: NettyChannelRegistry
) : HQModule {
    private val nettyEnabled: Boolean = config.getBoolean("netty.enabled")

    override fun onEnable() {
        if (nettyEnabled) {
            bootstrap.initializing()
            ProxyServer.getInstance().pluginManager.registerListener(plugin, PlayerConnectionListener(channelContainer))
        }
    }

    override fun onDisable() {
        bootstrap.shutdown()
    }
}