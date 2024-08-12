package kr.hqservice.framework.nms.extension

import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val itemService: NmsItemStackService by getKoin().inject(named("itemStack"))

fun ItemStack.nms(block: NmsItemStackWrapper.() -> Unit): ItemStack {
    val newItemMeta = itemService
        .wrap(this)
        .apply(block)
        .run(itemService::unwrap)
        .itemMeta

    this.setItemMeta(newItemMeta)
    return this
}

fun ItemStack.getNmsItemStack(): NmsItemStackWrapper {
    return itemService.wrap(this)
}

fun ItemStack.getNmsItemStack(block: NmsItemStackWrapper.() -> Unit): NmsItemStackWrapper {
    return itemService.wrap(this).apply(block)
}

fun ItemStack.getDisplayName(): String {
    return if (this.itemMeta?.hasDisplayName() == true) this.itemMeta!!.displayName else this.localizedName
}

fun ItemStack.setNmsItemStack(nmsItemStack: NmsItemStackWrapper) {
    itemMeta = itemService.unwrap(nmsItemStack).itemMeta
}

val ItemStack.localizedName: String
    get() = itemService.getItem(this).getLocalizedName() ?: type.name.lowercase()
