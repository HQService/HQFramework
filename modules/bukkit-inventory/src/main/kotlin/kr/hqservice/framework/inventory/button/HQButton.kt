package kr.hqservice.framework.inventory.button

import kr.hqservice.framework.inventory.container.HQContainer
import org.bukkit.inventory.ItemStack

interface HQButton {

    fun getItemStack(): ItemStack
    fun getDisplayName(): String
    fun getDescription(): List<String>
    fun isRemovable(): Boolean

    fun setSlot(container: HQContainer, vararg slots: Int)
    fun setSlot(container: HQContainer, vararg ranges: IntProgression)

}