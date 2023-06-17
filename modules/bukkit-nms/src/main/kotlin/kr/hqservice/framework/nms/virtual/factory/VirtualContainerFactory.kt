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
    private val messages: MutableList<Virtual> = mutableListOf()

    fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit = {}
    ) { messages.add(VirtualItem(receiver, slot, itemStack, itemEditBlock)) }

    fun setTitle(
        title: String
    ) { messages.add(VirtualContainer(receiver, title.colorize())) }

    fun getMessages(): Array<Virtual> {
        return messages.toTypedArray()
    }
}