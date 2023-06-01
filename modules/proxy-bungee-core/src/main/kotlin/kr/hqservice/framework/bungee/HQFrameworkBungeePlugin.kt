package kr.hqservice.framework.bungee

import kr.hqservice.framework.bungee.core.HQBungeePlugin
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.proxy.core.api.event.EventManager
import net.md_5.bungee.api.plugin.Plugin
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module

abstract class HQFrameworkBungeePlugin : HQBungeePlugin() {
    private val eventManager: EventManager by inject()

    final override fun getEventManager(): EventManager {
        return eventManager
    }

    final override fun onPreLoad() {
        startKoin()
    }

    private fun startKoin() {
        startKoin {
            val module = module {
                includes(HQFrameworkBungeeModule().module)
                single<Plugin>(named("hqframework")) { this@HQFrameworkBungeePlugin }
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkBungeePlugin }
                single<HQBungeePlugin>(named("hqframework")) { this@HQFrameworkBungeePlugin }
                single<HQFrameworkBungeePlugin> { this@HQFrameworkBungeePlugin }
            }
            modules(module)
        }
    }

    final override fun onPostDisable() {
        stopKoin()
    }
}