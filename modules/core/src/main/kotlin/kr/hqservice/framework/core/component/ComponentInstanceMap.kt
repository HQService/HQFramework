package kr.hqservice.framework.core.component

import kr.hqservice.framework.core.component.error.NotComponentException
import kotlin.reflect.KClass

class ComponentInstanceMap : MutableMap<KClass<*>, HQComponent> by mutableMapOf() {
    fun addSafely(instance: Any): HQComponent? {
        return if (instance is HQComponent) {
            put(instance::class, instance)
        } else {
            throw NotComponentException(instance::class)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : HQComponent> getComponent(key: KClass<T>): T {
        return get(key) as T
    }

    fun getComponents(): Collection<HQComponent> {
        return this.values
    }
}
