package kr.hqservice.framework.inventory.util

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

fun Inventory.hasSpace(size: Int = 1): Boolean {
    return this.storageContents.filter { it?.type?.isAir != false }.size >= size
}

fun PlayerInventory.hasSpace(vararg itemStack: ItemStack, repeat: Int = 1): Boolean {
    val inventoryCloned = Bukkit.getServer().createInventory(null, 36).apply {
        if (this@hasSpace.storageContents.isNotEmpty()) {
            this@hasSpace.storageContents.filter { it != null && !it.type.isAir }.forEachIndexed { index, itemStack ->
                setItem(index, itemStack.clone())
            }
        }
    }
    repeat(repeat) {
        val add = inventoryCloned.addItem(*itemStack.clone())
        if (add.isNotEmpty()) {
            return false
        }
    }
    return true
}