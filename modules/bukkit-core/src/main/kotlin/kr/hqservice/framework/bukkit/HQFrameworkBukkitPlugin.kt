package kr.hqservice.framework.bukkit

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.bukkit.core.component.registry.registry.InstanceFactoryRegistry
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import net.bytebuddy.agent.ByteBuddyAgent
import org.bukkit.plugin.Plugin
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module

abstract class HQFrameworkBukkitPlugin : HQBukkitPlugin() {
    //constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    final override fun onPreLoad() {
        try {
            ByteBuddyAgent.install()
        } catch (_: Exception) {
        }
        startKoin()
    }

    private fun startKoin() {
        startKoin {
            val module = module {
                single<Plugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQBukkitPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQFrameworkBukkitPlugin> { this@HQFrameworkBukkitPlugin }
                single { this@HQFrameworkBukkitPlugin.bukkitComponentRegistry } binds arrayOf(
                    InstanceFactoryRegistry::class,
                    ComponentRegistry::class
                )
                factory<BukkitComponentRegistry> { BukkitComponentRegistry(it.get()) }
            }
            modules(module)
        }
    }
}