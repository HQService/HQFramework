package kr.hqservice.framework.velocity.core.netty

import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.module.Module
import kr.hqservice.framework.velocity.core.component.module.Setup
import kr.hqservice.framework.velocity.core.component.module.Teardown
import kr.hqservice.framework.velocity.core.netty.listener.PlayerConnectionListener
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration

@Module
class NettyModule(
    private val plugin: HQVelocityPlugin,
    config: HQYamlConfiguration,
    private val bootstrap: NettyServerBootstrap,
    private val channelContainer: NettyChannelRegistry
) {
    private val isNettyEnabled = config.getBoolean("netty.enabled")

    @Setup
    fun setup() {
        if (state) return
        state = true
        if (isNettyEnabled) {
            bootstrap.initializing()
            val playerConnectionListener = PlayerConnectionListener(channelContainer)
            plugin.getProxyServer().eventManager.register(plugin, playerConnectionListener)
        }
    }

    @Teardown
    fun teardown() {
        bootstrap.shutdown()
    }

    companion object {
        private var state = false
    }
}
