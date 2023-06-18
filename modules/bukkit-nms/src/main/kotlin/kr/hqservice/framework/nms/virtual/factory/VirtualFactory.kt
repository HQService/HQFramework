package kr.hqservice.framework.nms.virtual.factory

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

interface VirtualFactory {
    @Deprecated("use inventory()")
    suspend fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    )

    suspend fun inventory(containerFactoryScope: VirtualContainerFactory.() -> Unit)

    suspend fun updateEntity(virtualEntity: AbstractVirtualEntity)
}