package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.inventory.ItemStack

class NmsItemStackWrapper(
    private val nmsItemStack: net.minecraft.world.item.ItemStack
) : NmsWrapper {
    private val tempBukkitItemStack = nmsItemStack.asBukkitCopy()

    fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit): NmsNBTTagCompoundWrapper {
        val tag = getTag()
        tag.tagScope()
        return tag
    }

    fun getTag(): NmsNBTTagCompoundWrapper {
        return NmsNBTTagCompoundWrapper(tempBukkitItemStack.itemMeta!!.persistentDataContainer)
    }

    fun getTagOrNull(): NmsNBTTagCompoundWrapper? {
        val pdc = tempBukkitItemStack.itemMeta!!.persistentDataContainer
        return if (pdc.isEmpty) null
        else NmsNBTTagCompoundWrapper(pdc)
    }

    fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?) {

    }

    fun getBukkitItemStack(): ItemStack {
        return nmsItemStack.asBukkitMirror().apply {
            itemMeta = tempBukkitItemStack.itemMeta
        }
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItemStack
    }
}