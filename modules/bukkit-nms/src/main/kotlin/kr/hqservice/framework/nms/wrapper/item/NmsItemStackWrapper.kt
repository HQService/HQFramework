package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.inventory.ItemStack

interface NmsItemStackWrapper : NmsWrapper {
    fun hasTag(): Boolean

    fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit = {}): NmsNBTTagCompoundWrapper

    fun getTag(): NmsNBTTagCompoundWrapper

    fun getTagOrNull(): NmsNBTTagCompoundWrapper?

    fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?)

    fun getItem(): NmsItemWrapper

    fun getBukkitItemStack(): ItemStack
}