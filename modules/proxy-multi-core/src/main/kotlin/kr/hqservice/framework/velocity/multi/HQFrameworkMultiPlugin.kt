package kr.hqservice.framework.velocity.multi

import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.velocity.HQFrameworkVelocityPlugin
import kr.hqservice.framework.velocity.multi.component.registry.MultiComponentRegistry

abstract class HQFrameworkMultiPlugin : HQFrameworkVelocityPlugin() {
    override val velocityComponentRegistry: ComponentRegistry by lazy { MultiComponentRegistry(this) }
}
