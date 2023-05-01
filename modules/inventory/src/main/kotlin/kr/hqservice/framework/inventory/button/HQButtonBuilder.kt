package kr.hqservice.framework.inventory.button

import kr.hqservice.framework.inventory.button.impl.HQButtonImpl
import kr.hqservice.framework.inventory.event.ButtonClickEvent
import kr.hqservice.framework.inventory.exception.IllegalMaterialException
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class HQButtonBuilder(
    private val itemStack: ItemStack,
    private val buildBlock: HQButtonBuilder.() -> Unit = {},
) {

    var displayName: String = itemStack.itemMeta?.displayName ?: ""
    var lore: List<String> = itemStack.itemMeta?.lore ?: emptyList()
    var itemFlags: Set<ItemFlag> = itemStack.itemMeta?.itemFlags ?: emptySet()
    var removable = false
    var glow = false
        set(value) {
            field = value
            if (value) itemStack.addUnsafeEnchantment(Enchantment.LURE, 1)
            else itemStack.removeEnchantment(Enchantment.LURE)
        }

    private var clickFunction: (ButtonClickEvent) -> Unit = {}

    constructor(material: Material, buildBlock: HQButtonBuilder.() -> Unit = {}) : this(material, 1, buildBlock)

    constructor(material: Material, amount: Int, buildBlock: HQButtonBuilder.() -> Unit = {}) : this(ItemStack(material, amount), buildBlock) {
        if (material == Material.AIR || !material.isItem) throw IllegalMaterialException(material)
    }

    @Deprecated(message = "setDurability")
    constructor(material: Material, amount: Int, data: Short) : this(material, amount) {
        itemStack.durability = data
    }

    fun clickFunc(block: (ButtonClickEvent) -> Unit) {
        clickFunction = block
    }

    fun build(): HQButton {
        buildBlock(this)
        itemStack.itemMeta = itemStack.itemMeta?.also { meta ->
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName))
            meta.lore = lore.map { ChatColor.translateAlternateColorCodes('&', it) }
            meta.addItemFlags(*itemFlags.toTypedArray())
        }
        return HQButtonImpl(itemStack, clickFunction, removable)
    }

}