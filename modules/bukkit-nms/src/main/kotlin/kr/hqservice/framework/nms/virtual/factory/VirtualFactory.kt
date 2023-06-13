package kr.hqservice.framework.nms.virtual.factory

import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.java.KoinJavaComponent

private val reflectionUtil: NmsReflectionUtil by KoinJavaComponent.getKoin().inject()

class VirtualFactory(val receiver: Player) {
    suspend fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ) {
        reflectionUtil.sendPacket(receiver, VirtualItem(receiver, slot, itemStack, itemEditBlock))
    }

    suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionUtil.sendPacket(receiver, virtualEntity)
    }
}