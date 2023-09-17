package kr.hqservice.framework.inventory.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.*

fun Inventory.hasSpace(size: Int = 1): Boolean {
    return this.storageContents.filter { it?.type?.isAir != false }.size >= size
}

fun PlayerInventory.hasSpace(vararg itemStack: ItemStack, repeat: Int = 1): Boolean {
    val inventoryCloned = Bukkit.getServer().createInventory(null, 45).apply {
        val uniqueItem = ItemStack(Material.STONE).apply {
            this.amount = 64
            val itemMeta = this.itemMeta?.apply { setDisplayName(UUID.randomUUID().toString()) }
            this.itemMeta = itemMeta
        }
        repeat(8) {
            addItem(uniqueItem)
        }
        if (this@hasSpace.extraContents.isNotEmpty()) {
            addItem(*this@hasSpace.extraContents.filter { it != null && !it.type.isAir }.toTypedArray())
        }
        if (this@hasSpace.storageContents.isNotEmpty()) {
            addItem(*this@hasSpace.storageContents.filter { it != null && !it.type.isAir }.toTypedArray())
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