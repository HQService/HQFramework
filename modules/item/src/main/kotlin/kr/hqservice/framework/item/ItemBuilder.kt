package kr.hqservice.framework.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.editMeta(block: ItemMeta.() -> Unit): ItemStack {
    itemMeta = itemMeta?.apply(block::invoke)
    return this
}

fun ItemMeta.addLine(line: String) {
    lore = lore?.apply {
        add(line)
    }?: mutableListOf(line)
}
