package kr.hqservice.framework.nms.virtual.factory

import kr.hqservice.framework.nms.virtual.Virtual
import org.bukkit.entity.Player

class VirtualFactory(val receiver: Player) {
    private val virtualList: MutableList<Virtual> = mutableListOf()

    internal fun addVirtualList(virtual: Virtual) {
        virtualList.add(virtual)
    }

    internal fun getVirtualList(): List<Virtual> {
        return virtualList
    }
}