package kr.hqservice.framework.nms.wrapper.item.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.item.ItemWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NmsItemWrapper(
    private val nmsItemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Any
) : ItemWrapper, KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))
    private val nmsItemClass = reflectionUtil.getNmsClass("Item", Version.V_15.handle("world.item"))

    private val getDescriptionIdFunction = reflectionUtil.getFunction(nmsItemClass, "j", listOf(nmsItemStackClass))

    fun getDescriptionName(): String {
        return getDescriptionIdFunction.call(nmsItem, nmsItemStackWrapper.nmsItemStack) as? String?: throw IllegalArgumentException()
    }

}