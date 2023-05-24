package kr.hqservice.framework.nms.service.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.item.impl.NmsNBTTagCompoundWrapper
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import kotlin.reflect.full.createInstance

@Component
@Named("tag")
@Singleton(binds = [NmsService::class])
class NmsNBTTagCompoundService(
    reflectionUtil: NmsReflectionUtil
) : KoinComponent, NmsService<Any?, NmsNBTTagCompoundWrapper>, HQService {
    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound",
        Version.V_15.handle("nbt"))

    override fun wrap(target: Any?): NmsNBTTagCompoundWrapper {
        return NmsNBTTagCompoundWrapper(target?: nbtTagClass.createInstance())
    }

    override fun unwrap(wrapper: NmsNBTTagCompoundWrapper): Any {
        return wrapper.nbtTag
    }
}
