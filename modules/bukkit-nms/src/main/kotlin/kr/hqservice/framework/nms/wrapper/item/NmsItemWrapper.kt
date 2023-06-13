package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kotlin.reflect.KClass

class NmsItemWrapper(
    private val nmsItemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Any,
    nmsItemStackClass: KClass<*>,
    nmsItemClass: KClass<*>,
    reflectionUtil: NmsReflectionUtil
) : NmsWrapper {
    private val getDescriptionIdFunction = reflectionUtil.getFunction(nmsItemClass, "j", listOf(nmsItemStackClass))

    fun getDescriptionName(): String {
        return getDescriptionIdFunction.call(nmsItem, nmsItemStackWrapper.getUnwrappedInstance()) as? String?: throw IllegalArgumentException()
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItem
    }

}