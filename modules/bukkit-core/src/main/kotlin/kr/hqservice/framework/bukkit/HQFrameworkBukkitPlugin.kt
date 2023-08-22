package kr.hqservice.framework.bukkit

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.bukkit.core.component.registry.registry.InstanceFactoryRegistry
import kr.hqservice.framework.global.core.HQPlugin
import net.bytebuddy.agent.ByteBuddyAgent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

abstract class HQFrameworkBukkitPlugin : HQBukkitPlugin {
    constructor() : super()
    constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    final override fun onPreLoad() {
        ByteBuddyAgent.install()
        startKoin()
    }

    private fun startKoin() {
        startKoin {
            val module = module {
                single<Plugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQBukkitPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQFrameworkBukkitPlugin> { this@HQFrameworkBukkitPlugin }
                factory<InstanceFactoryRegistry> { this@HQFrameworkBukkitPlugin.componentRegistry }
                factory<BukkitComponentRegistry> { BukkitComponentRegistry(it.get()) }
            }
            modules(module)
        }
    }
}