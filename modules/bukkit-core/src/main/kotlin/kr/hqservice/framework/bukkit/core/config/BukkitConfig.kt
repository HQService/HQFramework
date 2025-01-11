package kr.hqservice.framework.bukkit.core.config

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.ServicesManager

@Configuration
class BukkitConfig {
    @Bean
    fun provideServer(): Server {
        return Bukkit.getServer()
    }

    @Bean
    fun providePluginManager(server: Server): PluginManager {
        return server.pluginManager
    }

    @Bean
    fun provideServicesManager(server: Server): ServicesManager {
        return server.servicesManager
    }
}