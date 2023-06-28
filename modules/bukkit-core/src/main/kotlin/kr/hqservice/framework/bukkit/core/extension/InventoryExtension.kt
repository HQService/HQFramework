package kr.hqservice.framework.bukkit.core.extension

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun Inventory.getEmptySlots(): Collection<Int> {
    return storageContents.indices.filter { index ->
        storageContents[index] == null || storageContents[index].type == Material.AIR
    }
}

/*fun Inventory.isAddable(item: ItemStack): Boolean {
    return storageContents.any {
        it == null || it.type == Material.AIR || (it.isSimilar(item) && it.amount + item.amount <= it.maxStackSize)
    }
}*/
