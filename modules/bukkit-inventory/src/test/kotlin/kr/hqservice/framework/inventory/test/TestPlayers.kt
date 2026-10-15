package kr.hqservice.framework.inventory.test

import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

fun ServerMock.addPlayerWithCraftingView(name: String): Player {
    return addPlayer(name).also { player -> resetToCraftingView(player) }
}

fun ServerMock.resetToCraftingView(player: Player) {
    player.openInventory(createInventory(null, 9))
}

val Player.topHolder: InventoryHolder?
    get() {
        val topInventory: Inventory? = openInventory.topInventory
        return topInventory?.holder
    }
