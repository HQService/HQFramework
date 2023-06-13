package kr.hqservice.framework.nms.module

import kotlinx.coroutines.cancel
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import org.koin.core.annotation.Named

@Component
class NMSModule(
    @Named("virtual") private val virtualScope: HQCoroutineScope
) : HQModule {
    override fun onDisable() {
        virtualScope.cancel()
    }
}