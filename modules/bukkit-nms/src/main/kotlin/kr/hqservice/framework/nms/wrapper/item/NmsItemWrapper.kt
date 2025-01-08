package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.*

class NmsItemWrapper(
    private val nmsItemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Item,
    private val languageRegistry: LanguageRegistry
) : NmsWrapper {
    private fun getDescriptionName(): String {
        /*nmsItemStackWrapper.getUnwrappedInstance() as ItemStack*/
        return nmsItem.descriptionId
    }

    fun getLocalizedName(): String? {
        val locale = Locale.getDefault()
        return languageRegistry.getLocalizeValue(getDescriptionName())
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItem
    }
}