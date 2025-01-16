package kr.hqservice.framework.nms.virtual.item

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VirtualItem(
    private val player: Player,
    private val slot: Int,
    private val itemStack: ItemStack,
    private val itemEditBlock: ItemMeta.() -> Unit = {},
) : Virtual, KoinComponent {
    private val virtualItemMessageFactory: VirtualItemMessageFactory by inject()

    override fun createVirtualMessage(): VirtualMessage = virtualItemMessageFactory.create(
        player, slot, itemStack, itemEditBlock
    )
}