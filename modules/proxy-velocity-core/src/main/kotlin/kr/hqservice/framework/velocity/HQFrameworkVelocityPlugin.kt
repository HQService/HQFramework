package kr.hqservice.framework.velocity

import com.velocitypowered.api.proxy.ProxyServer
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.registry.VelocityComponentRegistry
import net.bytebuddy.agent.ByteBuddyAgent
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

abstract class HQFrameworkVelocityPlugin : HQVelocityPlugin() {
    final override fun onPreLoad() {
        ByteBuddyAgent.install()
        startKoin()
    }

    private fun startKoin() {
        org.koin.core.context.startKoin {
            val module = module {
                single<HQPlugin>(named("hqframework")) { this@HQFrameworkVelocityPlugin }
                single<HQVelocityPlugin>(named("hqframework")) { this@HQFrameworkVelocityPlugin }
                single<ProxyServer> { this@HQFrameworkVelocityPlugin.getProxyServer() }
                factory<ComponentRegistry> { VelocityComponentRegistry(it.get()) }
            }
            modules(module)
        }
    }

    final override fun onPostDisable() {
        stopKoin()
    }
}