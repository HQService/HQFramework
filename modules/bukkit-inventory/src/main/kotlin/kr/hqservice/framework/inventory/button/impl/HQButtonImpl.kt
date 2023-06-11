package kr.hqservice.framework.inventory.button.impl

import kr.hqservice.framework.inventory.button.HQButton
import kr.hqservice.framework.inventory.container.HQContainer
import kr.hqservice.framework.inventory.event.ButtonClickEvent
import org.bukkit.inventory.ItemStack

internal class HQButtonImpl(
    private val baseItemStack: ItemStack,
    private val clickFunction: (ButtonClickEvent)->Unit = {},
    private val removable: Boolean = false
): HQButton {
    override fun getItemStack(): ItemStack = baseItemStack

    override fun getDisplayName(): String {
        return baseItemStack.itemMeta?.displayName?: baseItemStack.type.name.lowercase()
    }

    override fun getDescription(): List<String> {
        return baseItemStack.itemMeta?.lore?: emptyList()
    }

    fun click(event: ButtonClickEvent) {
        clickFunction(event)
    }

    override fun isRemovable(): Boolean {
        return removable
    }

    override fun setSlot(container: HQContainer, vararg slots: Int) {
        slots.forEach {
            container.registerButton(it, this)
        }
    }

    override fun setSlot(container: HQContainer, vararg ranges: IntProgression) {
        ranges.forEach { range ->
            range.forEach {
                container.registerButton(it, this)
            }
        }
    }

}