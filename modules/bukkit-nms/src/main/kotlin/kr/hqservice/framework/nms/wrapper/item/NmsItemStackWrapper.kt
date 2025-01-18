package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.inventory.ItemStack

abstract class NmsItemStackWrapper : NmsWrapper {
    abstract fun hasTag(): Boolean

    abstract fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit = {}): NmsNBTTagCompoundWrapper

    abstract fun getTag(): NmsNBTTagCompoundWrapper

    abstract fun getTagOrNull(): NmsNBTTagCompoundWrapper?

    abstract fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?)

    abstract fun getItem(): NmsItemWrapper

    abstract fun getBukkitItemStack(): ItemStack
}