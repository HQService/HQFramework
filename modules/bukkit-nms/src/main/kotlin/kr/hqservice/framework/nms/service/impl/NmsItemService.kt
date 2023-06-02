package kr.hqservice.framework.nms.service.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.item.impl.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.impl.NmsItemWrapper
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import java.lang.UnsupportedOperationException

@Component
@Named("item")
@HQSingleton(binds = [NmsService::class])
class NmsItemService(
    reflectionUtil: NmsReflectionUtil
) : KoinComponent, NmsService<NmsItemStackWrapper, NmsItemWrapper>, HQService {

    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))

    private val getItemFunction = reflectionUtil.getFunction(nmsItemStackClass, "getItem",
        Version.V_15.handle("b"),
        Version.V_17.handle("c"))

    override fun wrap(target: NmsItemStackWrapper): NmsItemWrapper {
        return NmsItemWrapper(target, getItemFunction.call(target.nmsItemStack)?: throw IllegalArgumentException())
    }

    override fun unwrap(wrapper: NmsItemWrapper): NmsItemStackWrapper {
        throw UnsupportedOperationException()
    }

}
