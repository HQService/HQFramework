package kr.hqservice.framework.bungee

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.PluginManager
import java.util.logging.Logger

@Configuration
class HQFrameworkBungeeModule {
    @Bean
    fun provideProxyServer(): ProxyServer {
        return ProxyServer.getInstance()
    }

    @Bean
    fun providePluginManager(): PluginManager {
        return ProxyServer.getInstance().pluginManager
    }

    @Bean
    fun provideLogger(): Logger {
        return ProxyServer.getInstance().logger
    }
}