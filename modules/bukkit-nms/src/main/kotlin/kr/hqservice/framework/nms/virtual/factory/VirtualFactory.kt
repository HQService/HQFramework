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
    @Deprecated("use inventory()")
    suspend fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ) {
        reflectionWrapper.sendPacket(receiver,
            VirtualItem(receiver, slot, itemStack, itemEditBlock)
        )
    }

    suspend fun inventory(containerFactoryScope: VirtualContainerFactory.() -> Unit) {
        val containerFactory = VirtualContainerFactory(receiver)
        containerFactory.containerFactoryScope()
        reflectionWrapper.sendPacket(receiver, *containerFactory.getMessages())
    }

    suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(receiver, virtualEntity)
    }
}