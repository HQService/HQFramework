package kr.hqservice.framework

import kr.hqservice.framework.core.HQFrameworkModule
import kr.hqservice.framework.core.HQPlugin
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

@Suppress("unused")
class HQFrameworkPlugin : HQPlugin() {
    override fun onLoad() {
        startKoin {
            modules(HQFrameworkModule().module)
        }
    }
}