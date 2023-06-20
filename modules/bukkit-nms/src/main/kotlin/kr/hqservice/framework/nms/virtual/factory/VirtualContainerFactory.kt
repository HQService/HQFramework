package kr.hqservice.framework.nms.virtual.factory

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class VirtualContainerFactory(
    private val receiver: Player,
) {
    //private val setSlotItems = mutableMapOf<Int, VirtualItem>()
    private val slotItems = mutableListOf<VirtualItem>()
    private var titlePacket: VirtualContainer? = null

    fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ) {
        slotItems.add(VirtualItem(receiver, slot, itemStack, itemEditBlock))
        //setSlotItems[slot] = VirtualItem(receiver, slot, itemStack, itemEditBlock)
    }

    fun setTitle(title: String) { titlePacket = VirtualContainer(receiver, title.colorize()) }

    fun getMessages(): Array<Virtual> {
        val messages = mutableListOf<Virtual>()
        titlePacket?.apply(messages::add)
        messages.addAll(slotItems)
        /*receiver.openInventory.topInventory.contents.forEachIndexed { index, itemStack: ItemStack? ->
            setSlotItems[index]?: itemStack?.run {
                VirtualItem(receiver, index, this)
            }?.apply { messages.add(this) }
        }*/
        return messages.toTypedArray()
    }
}