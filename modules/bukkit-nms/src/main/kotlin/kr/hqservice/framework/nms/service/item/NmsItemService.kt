package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions

@Qualifier("item")
@Service
class NmsItemService(
    private val reflectionWrapper: NmsReflectionWrapper,
    private val languageRegistry: LanguageRegistry,
) : NmsService<NmsItemStackWrapper, NmsItemWrapper> {

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
        return NmsItemWrapper(
            target,
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
