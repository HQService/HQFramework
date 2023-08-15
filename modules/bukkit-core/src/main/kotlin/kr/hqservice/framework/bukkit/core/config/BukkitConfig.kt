package kr.hqservice.framework.bukkit.core.config

import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.Singleton
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.PluginManager

@Configuration
class BukkitConfig {
    @Singleton
    fun provideServer(): Server {
        return Bukkit.getServer()
    }

    @Singleton
    fun providePluginManager(): PluginManager {
        return Bukkit.getPluginManager()
    }
}