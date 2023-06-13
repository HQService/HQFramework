package kr.hqservice.framework.nms.extension

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val itemService: NmsService<ItemStack, NmsItemStackWrapper> by getKoin().inject(named("itemStack"))

fun ItemStack.nms(block: NmsItemStackWrapper.() -> Unit): ItemStack {
    itemMeta = itemService.wrap(this).apply(block).run(itemService::unwrap).itemMeta
    return this
}

fun ItemStack.getNmsItemStack(): NmsItemStackWrapper {
    return itemService.wrap(this)
}

fun ItemStack.getNmsItemStack(block: NmsItemStackWrapper.() -> Unit): NmsItemStackWrapper {
    return itemService.wrap(this).apply(block)
}

fun ItemStack.setNmsItemStack(nmsItemStack: NmsItemStackWrapper) {
    itemMeta = itemService.unwrap(nmsItemStack).itemMeta
}