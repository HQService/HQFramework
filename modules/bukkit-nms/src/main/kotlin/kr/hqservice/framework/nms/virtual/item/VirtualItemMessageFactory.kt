package kr.hqservice.framework.nms.virtual.item

import kr.hqservice.framework.nms.virtual.VirtualMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

interface VirtualItemMessageFactory {
    fun create(
        player: Player,
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ): VirtualMessage
}