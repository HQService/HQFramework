package kr.hqservice.framework.bukkit.core.netty

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.netty.handler.ChannelMainHandler
import kr.hqservice.framework.bukkit.core.netty.handler.impl.LocalChannelMainHandler
import kr.hqservice.framework.bukkit.core.netty.handler.impl.ProxiedChannelMainHandler
import kr.hqservice.framework.bukkit.core.netty.server.LocalNettyServer
import kr.hqservice.framework.bukkit.core.netty.server.ProxiedNettyServer
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.netty.api.NettyServer
import kr.hqservice.framework.netty.container.ChannelContainer
import org.bukkit.Server

@Configuration
class NettyConfig {
    @Bean
    fun provideNettyServer(
        channelContainer: ChannelContainer,
        server: Server,
        plugin: HQBukkitPlugin
    ): NettyServer {
        return if (plugin.config.getBoolean("netty.enabled")) {
            ProxiedNettyServer(channelContainer)
        } else {
            LocalNettyServer(server)
        }
    }

    @Bean
    fun provideChannelMainHandler(
        plugin: HQBukkitPlugin
    ) : ChannelMainHandler {
        return if (plugin.config.getBoolean("netty.enabled")) {
            ProxiedChannelMainHandler(plugin)
        } else {
            LocalChannelMainHandler(plugin)
        }
    }
}