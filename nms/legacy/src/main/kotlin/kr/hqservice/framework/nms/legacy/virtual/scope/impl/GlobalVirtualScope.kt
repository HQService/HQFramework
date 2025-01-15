package kr.hqservice.framework.nms.legacy.virtual.scope.impl

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.scope.AbstractVirtualScope
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player

open class GlobalVirtualScope(
    receivers: List<Player>,
    reflectionWrapper: NmsReflectionWrapper,
) : AbstractVirtualScope(receivers, reflectionWrapper) {
    private val groups = mutableMapOf<String, GroupVirtualScope>()

    override suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        if (groups.isNotEmpty()) {
            for ((index, group) in groups.values.withIndex()) {
                group.invokeFunctions()
                reflectionWrapper.sendPacket(group.viewers, virtualEntity, index == groups.size - 1)
            }
        } else super.updateEntity(virtualEntity)
    }

    fun defineGroup(groupName: String, predicate: (Player) -> Boolean) {
        val viewers = viewers.filter(predicate)
        if (viewers.isEmpty()) return

        groups.computeIfAbsent(groupName) {
            GroupVirtualScope(it, viewers, reflectionWrapper)
        }
    }

    fun group(groupName: String, groupVirtualScope: () -> Unit) {
        groups[groupName]?.addFunction(groupVirtualScope)
    }
}