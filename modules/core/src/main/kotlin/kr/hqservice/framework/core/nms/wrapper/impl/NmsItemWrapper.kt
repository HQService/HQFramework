package kr.hqservice.framework.core.nms.wrapper.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.util.getFunction
import kr.hqservice.framework.core.nms.wrapper.NmsWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NmsItemWrapper(
    private val nmsItemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Any
) : NmsWrapper, KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))
    private val nmsItemClass = reflectionUtil.getNmsClass("Item", Version.V_15.handle("world.item"))

    private val getDescriptionIdFunction = reflectionUtil.getFunction(nmsItemClass, "j", listOf(nmsItemStackClass))

    fun getDescriptionName(): String {
        return getDescriptionIdFunction.call(nmsItem, nmsItemStackWrapper.nmsItemStack) as? String?: throw IllegalArgumentException()
    }

}