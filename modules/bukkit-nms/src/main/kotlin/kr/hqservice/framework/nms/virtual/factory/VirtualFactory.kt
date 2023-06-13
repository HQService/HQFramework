package kr.hqservice.framework.nms.virtual.factory

import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class VirtualFactory(
    private val receiver: Player,
    private val reflectionWrapper: NmsReflectionWrapper
) {
    suspend fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ) {
        reflectionWrapper.sendPacket(receiver,
            VirtualItem(receiver, slot, itemStack, itemEditBlock)
        )
    }

    suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(receiver, virtualEntity)
    }
}