package kr.hqservice.framework.nms.extension

import kr.hqservice.framework.nms.NMSServiceManager
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.java.KoinJavaComponent.getKoin

private val itemService: NmsItemStackService by getKoin().inject()
private val nmsServiceManager: NMSServiceManager by getKoin().inject()

private val getItemNameMethod by lazy {
    try {
        ItemMeta::class.java.getDeclaredMethod("getItemName")
    } catch (_: Exception) { null }
}

fun ItemStack.nms(block: NmsItemStackWrapper.() -> Unit): ItemStack {
    itemMeta = itemService
        .wrap(this)
        .apply(block)
        .run(itemService::unwrap)
        .itemMeta
    return this
}

fun ItemStack.getNmsItemStack(): NmsItemStackWrapper {
    return itemService.wrap(this)
}

fun ItemStack.getNmsItemStack(block: NmsItemStackWrapper.() -> Unit): NmsItemStackWrapper {
    return itemService.wrap(this).apply(block)
}

fun ItemStack.getDisplayName(): String {
    return if (this.itemMeta?.hasDisplayName() == true) this.itemMeta!!.displayName else {
        if (nmsServiceManager.support(Version.V_21_4)) {
            this.itemMeta?.let { meta ->
                val result = getItemNameMethod?.invoke(meta) as? String
                if (result?.isEmpty() == true) null else result
            } ?: localizedName
        } else this.localizedName
    }
}

fun ItemStack.setNmsItemStack(nmsItemStack: NmsItemStackWrapper) {
    itemMeta = itemService.unwrap(nmsItemStack).itemMeta
}

val ItemStack.localizedName: String
    get() =
        itemService.wrap(this).getItem().getLocalizedName() ?: type.name.lowercase()
