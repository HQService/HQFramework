package kr.hqservice.framework.nms

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@Component
@ComponentScan
class NmsModule : HQModule {
    override fun onEnable() {

    }

    override fun onDisable() {

    }
}