package kr.hqservice.framework.inventory.container

import kr.hqservice.framework.inventory.button.impl.HQButtonImpl
import kr.hqservice.framework.inventory.handler.HQContainerHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.java.KoinJavaComponent.getKoin
import java.util.UUID

abstract class HQContainer(
    private val size: Int,
    private val title: String,
    private val cancel: Boolean = true,
) : InventoryHolder {
    private var baseInventory: Inventory? = null
    private val viewers = mutableListOf<UUID>()
    private val buttons = mutableMapOf<Int, HQButtonImpl>()
    private var plugin: Plugin? = null

    protected abstract fun initializing(inventory: Inventory)
    open fun onClick(event: InventoryClickEvent) {}
    open fun onDrag(event: InventoryDragEvent) {}
    open fun onClose(event: InventoryCloseEvent) {}

    internal fun removeViewer(uniqueId: UUID) {
        viewers.remove(uniqueId)
    }

    internal fun registerButton(slot: Int, button: HQButtonImpl) {
        if (buttons[slot] == button) return
        if (slot >= size) throw IndexOutOfBoundsException(slot)

        buttons[slot] = button
        val original = inventory.contents[slot]

        if (original?.isMatchedType(button.getItemStack()) == true)
            original.itemMeta = button.getItemStack().itemMeta
        else inventory.setItem(slot, button.getItemStack().clone())
        button.checkOwningPlayer(slot, inventory)
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
        for(player in players) {
            val prevHolder = player.openInventory.topInventory.holder
            if(prevHolder == this) continue
            if(prevHolder is HQContainer) {
                if(plugin == null) plugin = Bukkit.getPluginManager().getPlugin("HQFramework")
                val plugin = plugin!!
                plugin.server.scheduler.runTaskLater(plugin, Runnable { open(player) }, 1)
            } else player.openInventory(inventory)
        }
    }

    @Suppress("deprecation")
    private fun ItemStack.isMatchedType(other: ItemStack): Boolean {
        return type == other.type && durability == other.durability
    }
}