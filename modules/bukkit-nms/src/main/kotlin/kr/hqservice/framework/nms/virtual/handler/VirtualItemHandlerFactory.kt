package kr.hqservice.framework.nms.virtual.handler

import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.inventory.ItemStack

interface VirtualItemHandlerFactory {
    fun createHandler(
        itemStackService: NmsItemStackService,
        reflectionWrapper: NmsReflectionWrapper,
        targetContainer: Int,
        filter: (Int, ItemStack) -> Boolean,
        item: (index: Int, itemStack: ItemStack) -> Unit
    ): VirtualHandler
}