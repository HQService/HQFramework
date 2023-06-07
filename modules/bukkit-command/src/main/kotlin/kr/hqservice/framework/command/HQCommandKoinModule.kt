package kr.hqservice.framework.command

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQKoinModule
import kr.hqservice.framework.global.core.component.KoinModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@ComponentScan
@Component
@Module
class HQCommandKoinModule : HQKoinModule {
    override fun getModule(): KoinModule {
        return module
    }
}