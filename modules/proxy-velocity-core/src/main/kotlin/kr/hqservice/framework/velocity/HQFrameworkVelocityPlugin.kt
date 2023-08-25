package kr.hqservice.framework.velocity

import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.registry.VelocityComponentRegistry
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

abstract class HQFrameworkVelocityPlugin : HQVelocityPlugin() {
    final override fun onPreLoad() {
        startKoin()
    }

    private fun startKoin() {
        org.koin.core.context.startKoin {
            val module = module {
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkVelocityPlugin }
                single<HQVelocityPlugin>(named("hqframework")) { this@HQFrameworkVelocityPlugin }
                single<HQFrameworkVelocityPlugin> { this@HQFrameworkVelocityPlugin }
                single<ComponentRegistry> { VelocityComponentRegistry(it.get()) }
            }
            modules(module)
        }
    }

    final override fun onPostDisable() {
        stopKoin()
    }
}