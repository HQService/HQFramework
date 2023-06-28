package kr.hqservice.framework.inventory.button

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.inventory.button.impl.HQButtonImpl
import kr.hqservice.framework.inventory.event.ButtonClickEvent
import kr.hqservice.framework.inventory.exception.IllegalMaterialException
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

class HQButtonBuilder(
    original: ItemStack
) {
    private val itemStack = original.clone()
    private var displayName: String = itemStack.itemMeta?.displayName ?: ""
    private var lore: MutableList<String> = itemStack.itemMeta?.lore ?: mutableListOf()
    private var itemFlags: MutableSet<ItemFlag> = itemStack.itemMeta?.itemFlags ?.toMutableSet()?: mutableSetOf()
    private var customModelData: Int = if (itemStack.itemMeta?.hasCustomModelData() == true) { itemStack.itemMeta!!.customModelData } else { 0 }

    private var owningPlayer: UUID? = null
    private var removable = false

    private var itemMetaEditScope: ItemMeta.() -> Unit = {}

    var glow = false
        set(value) {
            field = value
            if (value) itemStack.addUnsafeEnchantment(Enchantment.LURE, 1)
            else itemStack.removeEnchantment(Enchantment.LURE)
        }

    private var clickFunction: (ButtonClickEvent)->Unit = {}

    constructor(material: Material) : this(material, 1)

    constructor(material: Material, amount: Int) : this(ItemStack(material, amount)) {
        if (material == Material.AIR || !material.isItem) throw IllegalMaterialException(material)
    }

    @Deprecated(message = "setDurability")
    constructor(material: Material, amount: Int, data: Short) : this(material, amount) {
        itemStack.durability = data
    }

    fun setOwningPlayer(uniqueId: UUID): HQButtonBuilder {
        owningPlayer = uniqueId
        return this
    }

    fun setDisplayName(name: String): HQButtonBuilder {
        displayName = name
        return this
    }

    fun setLore(lore: List<String>): HQButtonBuilder {
        this.lore.clear()
        this.lore.addAll(lore.map { it.colorize() })
        return this
    }

    fun addLore(lore: String): HQButtonBuilder {
        this.lore.add(lore.colorize())
        return this
    }

    fun getLore(): List<String> {
        return lore
    }

    fun setCustomModelData(customModelData: Int): HQButtonBuilder {
        this.customModelData = customModelData
        return this
    }

    fun addItemFlags(vararg itemFlag: ItemFlag): HQButtonBuilder {
        itemFlags.addAll(itemFlag)
        return this
    }

    fun setRemovable(removable: Boolean): HQButtonBuilder {
        this.removable = removable
        return this
    }

    fun setClickFunction(block: (ButtonClickEvent) -> Unit): HQButtonBuilder {
        clickFunction = block
        return this
    }

    fun setGlow(glow: Boolean): HQButtonBuilder {
        this.glow = glow
        return this
    }

    fun setMeta(scope: ItemMeta.() -> Unit): HQButtonBuilder {
        this.itemMetaEditScope = scope
        return this
    }

    fun build(): HQButton {
        val scope: (ItemMeta) -> Unit = { meta ->
            meta.setDisplayName(displayName.colorize())
            meta.lore = lore.map { it.colorize() }
            meta.addItemFlags(*itemFlags.toTypedArray())
            if (meta.hasCustomModelData()) {
                meta.setCustomModelData(customModelData)
            }
            meta.itemMetaEditScope()
        }
        if(owningPlayer != null && itemStack.type != Material.PLAYER_HEAD) {
            owningPlayer = null
        }
        itemStack.itemMeta = itemStack.itemMeta?.also(scope)
        return HQButtonImpl(itemStack, clickFunction, removable, owningPlayer, scope)
    }
}