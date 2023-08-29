package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.handler.impl.VirtualItemHandler
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack

class VirtualViewScope(
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper>,
    private val reflectionWrapper: NmsReflectionWrapper,
    private val containerId: Int
) {
    private var filter: (Int, ItemStack) -> Boolean = { _, _ -> true }
    private var item: (Int, ItemStack) -> Unit = { _, _ -> }

    fun condition(filter: (slot: Int, original: ItemStack) -> Boolean) {
        this.filter = filter
    }

    fun item(itemStackScope: (slot: Int, itemStack: ItemStack) -> Unit) {
        this.item = itemStackScope
    }

    internal fun create(): VirtualHandler {
        return VirtualItemHandler(itemStackService, reflectionWrapper, containerId, filter, item)
    }
}