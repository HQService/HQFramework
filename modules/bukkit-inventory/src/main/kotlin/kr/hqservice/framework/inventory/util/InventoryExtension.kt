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

fun PlayerInventory.hasSpace(vararg itemStack: ItemStack): Boolean {
    val inventoryCloned = Bukkit.getServer().createInventory(null, 45).apply {
        val uniqueItem = ItemStack(Material.STONE).apply {
            this.amount = 64
            val itemMeta = this.itemMeta?.apply { setDisplayName(UUID.randomUUID().toString()) }
            this.itemMeta = itemMeta
        }
        repeat(8) {
            addItem(uniqueItem)
        }
        addItem(*this@hasSpace.extraContents)
        addItem(*this@hasSpace.storageContents)
    }
    val add = inventoryCloned.addItem(*itemStack.clone())
    return add.isEmpty()
}