package kr.hqservice.framework.nms

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.component.KoinComponent
import org.koin.ksp.generated.module

@Module
@Component
@ComponentScan
class NmsModule : HQModule, KoinComponent {
    override fun onEnable() {
        getKoin().loadModules(listOf(module))
    }

    override fun onDisable() {
        getKoin().unloadModules(listOf(module))
    }
}