package kr.hqservice.framework.bukkit.core.component.handler

import kr.hqservice.framework.bukkit.core.component.HQInstanceFactory
import kr.hqservice.framework.bukkit.core.component.registry.InstanceFactoryRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler

@ComponentHandler
class InstanceFactoryComponentHandler(private val registry: InstanceFactoryRegistry) :
    HQComponentHandler<HQInstanceFactory<*>> {
    override fun setup(element: HQInstanceFactory<*>) {
        registry.registerInstanceFactory(element)
    }
}