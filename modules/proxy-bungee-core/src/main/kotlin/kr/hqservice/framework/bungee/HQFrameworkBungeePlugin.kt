package kr.hqservice.framework.bungee

import kr.hqservice.framework.bungee.core.HQBungeePlugin
import kr.hqservice.framework.global.core.HQPlugin
import net.bytebuddy.agent.ByteBuddyAgent
import net.md_5.bungee.api.plugin.Plugin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module

abstract class HQFrameworkBungeePlugin : HQBungeePlugin() {
    final override fun onPreLoad() {
        ByteBuddyAgent.install()
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