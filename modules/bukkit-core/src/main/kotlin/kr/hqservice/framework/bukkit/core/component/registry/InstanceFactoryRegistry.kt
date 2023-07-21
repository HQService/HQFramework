package kr.hqservice.framework.bukkit.core.component.registry

import kr.hqservice.framework.bukkit.core.component.HQInstanceFactory

interface InstanceFactoryRegistry {
    fun <T> registerInstanceFactory(instanceFactory: HQInstanceFactory<T>)
}