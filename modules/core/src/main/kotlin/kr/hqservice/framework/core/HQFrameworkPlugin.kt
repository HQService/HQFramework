package kr.hqservice.framework.core

import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class HQFrameworkPlugin : HQPlugin() {
    override fun onLoad() {
        startKoin {
            modules(HQFrameworkModule().module)
        }
    }
}