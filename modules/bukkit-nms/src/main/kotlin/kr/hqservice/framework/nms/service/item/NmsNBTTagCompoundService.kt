package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import org.koin.core.annotation.Named
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Component
@Named("tag")
@HQSingleton(binds = [NmsService::class])
class NmsNBTTagCompoundService(
    private val reflectionWrapper: NmsReflectionWrapper
) : NmsService<Any?, NmsNBTTagCompoundWrapper> {
    private val nbtTagClass = reflectionWrapper.getNmsClass(
        "NBTTagCompound",
        Version.V_15.handle("nbt")
    )

    override fun wrap(target: Any?): NmsNBTTagCompoundWrapper {
        return NmsNBTTagCompoundWrapper(target ?: nbtTagClass.createInstance(), reflectionWrapper)
    }

    override fun unwrap(wrapper: NmsNBTTagCompoundWrapper): Any {
        return wrapper.getUnwrappedInstance()
    }

    override fun getOriginalClass(): KClass<*> {
        return nbtTagClass
    }

    override fun getTargetClass(): KClass<*> {
        return nbtTagClass
    }
}
