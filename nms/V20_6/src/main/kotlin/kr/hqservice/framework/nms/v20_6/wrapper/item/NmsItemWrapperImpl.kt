package kr.hqservice.framework.nms.v20_6.wrapper.item

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import net.minecraft.world.item.Item
import java.util.*

class NmsItemWrapperImpl(
    val itemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Item,
    private val languageRegistry: LanguageRegistry
) : NmsItemWrapper() {
    override fun getDescriptionName(): String {
        return nmsItem.descriptionId
    }

    override fun getLocalizedName(): String? {
        val locale = Locale.getDefault()
        return languageRegistry.getLocalizeValue(getDescriptionName())
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItem
    }
}