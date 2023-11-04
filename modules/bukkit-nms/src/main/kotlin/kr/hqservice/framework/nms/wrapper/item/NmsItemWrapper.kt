package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import java.util.*
import kotlin.reflect.KClass

class NmsItemWrapper(
    private val nmsItemStackWrapper: NmsItemStackWrapper,
    private val nmsItem: Any,
    nmsItemStackClass: KClass<*>,
    nmsItemClass: KClass<*>,
    reflectionWrapper: NmsReflectionWrapper,
    private val languageRegistry: LanguageRegistry
) : NmsWrapper {

    private val getDescriptionIdFunction = reflectionWrapper.getFunction(nmsItemClass, "getDescriptionId", listOf(nmsItemStackClass),
        Version.V_17.handleFunction("j") { setParameterClasses(nmsItemStackClass) },
        Version.V_17_FORGE.handleFunction("m_5671_") { setParameterClasses(nmsItemStackClass) } // ~1.20.2
    )

    fun getDescriptionName(): String {
        return getDescriptionIdFunction.call(nmsItem, nmsItemStackWrapper.getUnwrappedInstance()) as? String
            ?: throw IllegalArgumentException()
    }

    fun getLocalizedName(): String? {
        val locale = Locale.getDefault()
        return languageRegistry.getLocalizeValue(getDescriptionName())
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItem
    }
}