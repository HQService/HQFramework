package kr.hqservice.framework.core.extension

import kr.hqservice.framework.core.nms.service.impl.NmsItemStackService
import kr.hqservice.framework.core.nms.service.impl.NmsNBTTagCompoundService
import kr.hqservice.framework.core.nms.wrapper.impl.NmsItemStackWrapper
import kr.hqservice.framework.core.nms.wrapper.impl.NmsNBTTagCompoundWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val itemService: NmsItemStackService by getKoin().inject(named("item"))
private val tagService: NmsNBTTagCompoundService by getKoin().inject(named("tag"))

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

fun NmsItemStackWrapper.tag(block: NmsNBTTagCompoundWrapper.() -> Unit) {
    setTag(tagService.wrap(getTag()).apply(block))
}