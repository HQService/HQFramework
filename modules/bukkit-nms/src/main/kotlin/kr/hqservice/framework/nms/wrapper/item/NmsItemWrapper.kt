package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kotlin.reflect.KClass

class NmsItemWrapper(
    private val nmsItemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Any,
    nmsItemStackClass: KClass<*>,
    nmsItemClass: KClass<*>,
    reflectionWrapper: NmsReflectionWrapper
) : NmsWrapper {
    private val getDescriptionIdFunction = reflectionWrapper.getFunction(nmsItemClass, "j", listOf(nmsItemStackClass))

    fun getDescriptionName(): String {
        return getDescriptionIdFunction.call(nmsItem, nmsItemStackWrapper.getUnwrappedInstance()) as? String?: throw IllegalArgumentException()
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItem
    }

}