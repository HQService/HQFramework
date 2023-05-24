package kr.hqservice.framework.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.PluginManager
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.util.logging.Logger

@ComponentScan
@Module
class HQFrameworkBungeeModule {
    @Single
    fun provideProxyServer(): ProxyServer {
        return ProxyServer.getInstance()
    }

    @Single
    fun providePluginManager(): PluginManager {
        return ProxyServer.getInstance().pluginManager
    }

    @Single
    fun provideLogger(): Logger {
        return ProxyServer.getInstance().logger
    }
}