package kr.hqservice.framework.nms.legacy.virtual.scope.impl

import kr.hqservice.framework.nms.virtual.scope.AbstractVirtualScope
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player

class GroupVirtualScope(
    val groupName: String,
    receivers: List<Player>,
    reflectionWrapper: NmsReflectionWrapper
) : AbstractVirtualScope(receivers, reflectionWrapper) {
    private val functions = mutableListOf<() -> Unit>()

    internal fun addFunction(function: () -> Unit) {
        functions.add(function)
    }

    internal fun invokeFunctions() {
        if (functions.isEmpty()) return

        for (function in functions) {
            function.invoke()
        }
        functions.clear()
    }
}