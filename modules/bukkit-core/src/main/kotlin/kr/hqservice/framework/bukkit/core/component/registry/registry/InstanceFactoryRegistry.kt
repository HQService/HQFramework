package kr.hqservice.framework.bukkit.core.component.registry.registry

import kr.hqservice.framework.bukkit.core.component.registry.HQInstanceFactory

interface InstanceFactoryRegistry {
    fun <T> registerInstanceFactory(instanceFactory: HQInstanceFactory<T>)

    fun <T> unregisterInstanceFactory(instanceFactory: HQInstanceFactory<T>)
}