package kr.hqservice.framework.nms.virtual.factory

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class VirtualContainerFactory(
    private val receiver: Player,
) : KoinComponent {
    //private val setSlotItems = mutableMapOf<Int, VirtualItem>()
    private val slotItems = mutableListOf<VirtualItem>()
    private var titlePacket: VirtualContainer? = null

    fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ) {
        //slotItems.add(VirtualItem(receiver, slot, itemStack, itemEditBlock))
        slotItems.add(VirtualItem(receiver, slot, itemStack, itemEditBlock))
        //setSlotItems[slot] = VirtualItem(receiver, slot, itemStack, itemEditBlock)
    }

    fun setTitle(title: String) {
        titlePacket = VirtualContainer(receiver, title.colorize())
    }

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