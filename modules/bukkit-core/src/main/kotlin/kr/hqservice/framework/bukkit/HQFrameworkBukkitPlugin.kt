package kr.hqservice.framework.bukkit

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.bukkit.core.component.registry.registry.InstanceFactoryRegistry
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import java.io.File

abstract class HQFrameworkBukkitPlugin : HQBukkitPlugin {
    constructor() : super()
    protected constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File
    ) : super(loader, description, dataFolder, file)

    final override fun onPreLoad() {
        startKoin()
    }

    private fun startKoin() {
        startKoin {
            val module = module {
                single<Plugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQBukkitPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQFrameworkBukkitPlugin> { this@HQFrameworkBukkitPlugin }
                factory<BukkitComponentRegistry> { BukkitComponentRegistry(it.get()) }
                single<ComponentRegistry> { this@HQFrameworkBukkitPlugin.bukkitComponentRegistry } binds arrayOf(InstanceFactoryRegistry::class)
            }
            modules(module)
        }
    }

    final override fun onPostDisable() {
        stopKoin()
    }
}