package kr.hqservice.framework.velocity.multi.component.registry

import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.registry.VelocityComponentRegistry
import kr.hqservice.framework.velocity.core.netty.NettyModule
import kr.hqservice.framework.velocity.core.netty.api.ProxyNettyServer
import kr.hqservice.framework.velocity.core.netty.registry.impl.NettyChannelRegistryImpl

class MultiComponentRegistry(plugin: HQVelocityPlugin) : VelocityComponentRegistry(plugin) {
    override fun filterComponent(clazz: Class<*>): Boolean {
        return clazz !in replacedComponents
    }

    private companion object {
        val replacedComponents: Set<Class<*>> = setOf(
            NettyModule::class.java,
            NettyChannelRegistryImpl::class.java,
            ProxyNettyServer::class.java
        )
    }
}
