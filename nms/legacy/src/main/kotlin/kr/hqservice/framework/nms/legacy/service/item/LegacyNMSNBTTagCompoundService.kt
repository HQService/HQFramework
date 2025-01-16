package kr.hqservice.framework.nms.legacy.service.item

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.item.LegacyNmsNBTTagCompoundWrapper
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Service
@Qualifier("tag")
class LegacyNMSNBTTagCompoundService(
    reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsNBTTagCompoundService {
    private val nbtTagClass = reflectionWrapper.getNmsClass("NBTTagCompound",
        Version.V_17.handle("nbt")
    )

    override fun wrap(target: Any?): NmsNBTTagCompoundWrapper {
        return LegacyNmsNBTTagCompoundWrapper(target ?: nbtTagClass.createInstance())
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
