package kr.hqservice.framework

import kr.hqservice.framework.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.HQPlugin
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module
import java.io.File

abstract class HQFrameworkBukkitPlugin : HQBukkitPlugin {
    constructor() : super()
    constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    final override fun onPreLoad() {
        startKoin()
    }

    private fun startKoin() {
        startKoin {
            val module = module {
                includes(HQFrameworkBukkitModule().module)
                single<Plugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQBukkitPlugin>(named("hqframework")) { this@HQFrameworkBukkitPlugin }
                single<HQFrameworkBukkitPlugin> { this@HQFrameworkBukkitPlugin }
            }
            modules(module)
        }
    }

    final override fun onPostDisable() {
        stopKoin()
    }
}