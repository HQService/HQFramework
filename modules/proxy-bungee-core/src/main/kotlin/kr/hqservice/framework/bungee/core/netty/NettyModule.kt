package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.bungee.core.component.module.Module
import kr.hqservice.framework.bungee.core.component.module.Setup
import kr.hqservice.framework.bungee.core.component.module.Teardown
import kr.hqservice.framework.bungee.core.netty.listener.PlayerConnectionListener
import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

@Module
class NettyModule(
    config: HQYamlConfiguration,
    private val plugin: Plugin,
    private val bootstrap: NettyServerBootstrap,
    private val channelContainer: NettyChannelRegistry
) {
    private val nettyEnabled: Boolean = config.getBoolean("netty.enabled")

    @Setup
    fun setup() {
        if (nettyEnabled) {
            bootstrap.initializing()
            ProxyServer.getInstance().pluginManager.registerListener(plugin, PlayerConnectionListener(channelContainer))
        }
    }

    @Teardown
    fun teardown() {
        bootstrap.shutdown()
    }
}