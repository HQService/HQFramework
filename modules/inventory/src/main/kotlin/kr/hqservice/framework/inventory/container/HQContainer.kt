package kr.hqservice.framework.inventory.container

import kr.hqservice.framework.inventory.button.HQButton
import kr.hqservice.framework.inventory.button.impl.HQButtonImpl
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

abstract class HQContainer(
    private val size: Int,
    private val title: String,
    private val cancel: Boolean = true,
) : InventoryHolder {

    private var baseInventory: Inventory? = null
    private val buttons = mutableMapOf<Int, HQButtonImpl>()

    protected abstract fun initializing(inventory: Inventory)
    open fun onClick(event: InventoryClickEvent) {}
    open fun onDrag(event: InventoryDragEvent) {}
    open fun onClose(event: InventoryCloseEvent) {}

    internal fun registerButton(slot: Int, button: HQButtonImpl) {
        if (buttons[slot] == button) return
        if (slot >= size) throw IndexOutOfBoundsException(slot)

        buttons[slot] = button
        val original = inventory.contents[slot]

        if (original?.isMatchedType(button.getItemStack()) == true)
            original.itemMeta = button.getItemStack().itemMeta
        else inventory.contents[slot] = button.getItemStack().clone()
    }

    internal fun getButton(slot: Int): HQButtonImpl? = buttons[slot]

    final override fun getInventory(): Inventory {
        return baseInventory ?: Bukkit.createInventory(
            this, size,
            ChatColor.translateAlternateColorCodes('&', title)
        ).apply {
            baseInventory = this
            initializing(this)
        }
    }

    fun isCancelled(): Boolean = cancel

    fun refresh() {
        baseInventory?.apply {
            buttons.filter { it.value.isRemovable() }
                .forEach { inventory.setItem(it.key, null) }
            buttons.entries.removeIf { it.value.isRemovable() }
            initializing(this)
        }
    }

    fun open(vararg players: Player) {
        for(player in players)
            player.openInventory(inventory)
    }

    private fun ItemStack.isMatchedType(other: ItemStack): Boolean {
        return type == other.type && durability == other.durability
    }

}