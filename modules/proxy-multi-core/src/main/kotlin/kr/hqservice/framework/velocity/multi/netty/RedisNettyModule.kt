package kr.hqservice.framework.velocity.multi.netty

import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.module.Module
import kr.hqservice.framework.velocity.core.component.module.Setup
import kr.hqservice.framework.velocity.core.component.module.Teardown
import kr.hqservice.framework.velocity.core.netty.NettyServerBootstrap
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.velocity.multi.netty.listener.RedisPlayerConnectionListener
import kr.hqservice.framework.yaml.config.HQYamlConfiguration

@Module
class RedisNettyModule(
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
            val playerConnectionListener = RedisPlayerConnectionListener(channelContainer)
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
