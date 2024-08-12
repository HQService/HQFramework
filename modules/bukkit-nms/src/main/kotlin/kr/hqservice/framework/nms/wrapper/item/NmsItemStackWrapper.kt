package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

class NmsItemStackWrapper(
    private val nmsItemStack: net.minecraft.world.item.ItemStack
) : NmsWrapper {
    private val tempBukkitItemStack = nmsItemStack.asBukkitCopy()
    private val tempTag = tempBukkitItemStack.itemMeta?.persistentDataContainer.run { NmsNBTTagCompoundWrapper(this) }

    fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit): NmsNBTTagCompoundWrapper {
        val tag = tempTag
        tag.tagScope()
        return tag
    }

    fun getTag(): NmsNBTTagCompoundWrapper {
        return tempTag
    }

    fun getTagOrNull(): NmsNBTTagCompoundWrapper? {
        val pdc = tempTag
        return if (pdc.isEmpty()) null
        else pdc
    }

    fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?) {}

    fun getBukkitItemStack(): ItemStack {
        return tempBukkitItemStack.apply {
            itemMeta = itemMeta?.apply {
                if (tempTag.getUnwrappedInstance() !is Int)
                    persistentDataContainer.readFromBytes((tempTag.getUnwrappedInstance() as PersistentDataContainer).serializeToBytes())
            }
        }
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItemStack
    }
}