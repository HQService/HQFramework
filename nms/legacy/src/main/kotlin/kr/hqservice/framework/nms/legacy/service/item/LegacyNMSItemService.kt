package kr.hqservice.framework.nms.legacy.service.item

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.item.LegacyNmsItemStackWrapper
import kr.hqservice.framework.nms.legacy.wrapper.item.LegacyNmsItemWrapper
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import kotlin.reflect.KClass

@Qualifier("item")
@Service
class LegacyNMSItemService(
    private val reflectionWrapper: LegacyNmsReflectionWrapper,
    private val languageRegistry: LanguageRegistry,
) : NmsItemService {
    private val nmsItemStackClass = reflectionWrapper.getNmsClass("ItemStack",
        Version.V_17.handle("world.item")
    )
    private val nmsItemClass = reflectionWrapper.getNmsClass("Item",
        Version.V_17.handle("world.item")
    )
    private val getItemFunction = reflectionWrapper.getFunction(nmsItemStackClass, "getItem",
        Version.V_18.handle("c"),
        Version.V_20.handle("d"),
        Version.V_17_FORGE.handle("m_41720_"), // ~1.20.2
    )

    override fun wrap(target: NmsItemStackWrapper): NmsItemWrapper {
        return LegacyNmsItemWrapper(
            target as LegacyNmsItemStackWrapper,
            getItemFunction.call(target.getUnwrappedInstance()) ?: throw IllegalArgumentException(),
            nmsItemStackClass,
            nmsItemClass,
            reflectionWrapper,
            languageRegistry
        )
    }

    override fun unwrap(wrapper: NmsItemWrapper): NmsItemStackWrapper {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return nmsItemStackClass
    }

    override fun getTargetClass(): KClass<*> {
        return nmsItemClass
    }

}
