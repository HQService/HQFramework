package kr.hqservice.framework.core.component

import kr.hqservice.framework.core.component.error.NotComponentException

class ComponentInstanceList : MutableList<HQComponent> by mutableListOf() {
    fun addSafely(instance: Any): Boolean {
        return if (instance is HQComponent) {
            add(instance)
        } else {
            throw NotComponentException(instance::class)
        }
    }
}
