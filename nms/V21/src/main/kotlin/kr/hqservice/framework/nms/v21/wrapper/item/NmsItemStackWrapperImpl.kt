package kr.hqservice.framework.nms.v21.wrapper.item

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

class NmsItemStackWrapperImpl(
    private val languageRegistry: LanguageRegistry,
    private val nmsItemStack: net.minecraft.world.item.ItemStack,
) : NmsItemStackWrapper() {
    private val tempBukkitItemStack = nmsItemStack.asBukkitCopy()
    private val tempTag = tempBukkitItemStack.itemMeta?.persistentDataContainer.run { NmsNBTTagCompoundWrapperImpl(this) }

    override fun hasTag(): Boolean {
        return !tempTag.isEmpty()
    }

    override fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit): NmsNBTTagCompoundWrapper {
        val tag = tempTag
        tag.tagScope()
        return tag
    }

    override fun getTag(): NmsNBTTagCompoundWrapper {
        return tempTag
    }

    override fun getTagOrNull(): NmsNBTTagCompoundWrapper? {
        val pdc = tempTag
        return if (pdc.isEmpty()) null
        else pdc
    }

    override fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?) {}

    override fun getItem(): NmsItemWrapper {
        return NmsItemWrapperImpl(this, nmsItemStack.item, languageRegistry)
    }

    override fun getBukkitItemStack(): ItemStack {
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