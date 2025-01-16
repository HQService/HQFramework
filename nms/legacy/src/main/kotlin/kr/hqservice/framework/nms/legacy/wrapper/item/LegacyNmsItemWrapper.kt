package kr.hqservice.framework.nms.legacy.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import java.util.*
import kotlin.reflect.KClass

class LegacyNmsItemWrapper(
    private val nmsItemStackWrapper: LegacyNmsItemStackWrapper,
    private val nmsItem: Any,
    nmsItemStackClass: KClass<*>,
    nmsItemClass: KClass<*>,
    reflectionWrapper: NmsReflectionWrapper,
    private val languageRegistry: LanguageRegistry
) : NmsItemWrapper {

    private val getDescriptionIdFunction = reflectionWrapper.getFunction(nmsItemClass, "getDescriptionId", listOf(nmsItemStackClass),
        Version.V_17.handleFunction("j") { setParameterClasses(nmsItemStackClass) },
        Version.V_17_FORGE.handleFunction("m_5671_") { setParameterClasses(nmsItemStackClass) } // ~1.20.2
    )

    override fun getDescriptionName(): String {
        return getDescriptionIdFunction.call(nmsItem, nmsItemStackWrapper.getUnwrappedInstance()) as? String
            ?: throw IllegalArgumentException()
    }

    override fun getLocalizedName(): String? {
        val locale = Locale.getDefault()
        return languageRegistry.getLocalizeValue(getDescriptionName())
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItem
    }
}