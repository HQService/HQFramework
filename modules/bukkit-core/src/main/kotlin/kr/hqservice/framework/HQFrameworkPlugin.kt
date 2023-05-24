package kr.hqservice.framework

import kr.hqservice.framework.core.HQFrameworkModule
import kr.hqservice.framework.core.HQPlugin
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module
import java.io.File

abstract class HQFrameworkPlugin : HQPlugin {
    constructor() : super()
    constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    final override fun onLoad() {
        startKoin()
    }

    private fun startKoin() {
        startKoin {
            val module = module {
                includes(HQFrameworkModule().module)
                single<Plugin>(named("hqframework")) { this@HQFrameworkPlugin }
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkPlugin }
                single<HQFrameworkPlugin>(named("hqframework")) { this@HQFrameworkPlugin }
            }
            modules(module)
        }
    }

    final override fun onPostDisable() {
        stopKoin()
    }
}