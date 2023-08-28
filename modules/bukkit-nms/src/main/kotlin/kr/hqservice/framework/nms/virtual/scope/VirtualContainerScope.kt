package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.core.component.KoinComponent

class VirtualContainerScope(
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
        titlePacket = VirtualContainer(receiver, "{\"text\": \"§f$${title.colorize()}\"}")
    }

    fun setTitle(title: BaseComponent) {
        titlePacket = VirtualContainer(receiver, ComponentSerializer.toString(title))
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